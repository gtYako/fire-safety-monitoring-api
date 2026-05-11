package com.firesafety.config;

import com.firesafety.entity.*;
import com.firesafety.enums.SensorStatus;
import com.firesafety.enums.SensorType;
import com.firesafety.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final SensorRepository sensorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing application data...");

        Map<String, Permission> permissions = createPermissions();
        Map<String, Role> roles = createRoles(permissions);
        createAdmin(roles);
        createTestData();

        log.info("Data initialization complete.");
    }

    private Map<String, Permission> createPermissions() {
        String[] permNames = {
            "USER_READ", "USER_MANAGE",
            "BUILDING_READ", "BUILDING_MANAGE",
            "ROOM_READ", "ROOM_MANAGE",
            "SENSOR_READ", "SENSOR_CREATE", "SENSOR_UPDATE", "SENSOR_DELETE",
            "READING_READ", "READING_CREATE",
            "ALERT_READ", "ALERT_UPDATE",
            "INCIDENT_READ", "INCIDENT_UPDATE",
            "FILE_UPLOAD", "REPORT_GENERATE", "IMPORT_DATA"
        };

        Map<String, Permission> perms = new HashMap<>();
        for (String name : permNames) {
            Permission p = permissionRepository.findByName(name)
                    .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
            perms.put(name, p);
        }
        log.info("Created {} permissions", perms.size());
        return perms;
    }

    private Map<String, Role> createRoles(Map<String, Permission> perms) {
        Map<String, Role> roles = new HashMap<>();

        // ADMIN - all permissions
        roles.put("ADMIN", createRole("ADMIN", new HashSet<>(perms.values())));

        // DISPATCHER - monitor and handle alerts
        roles.put("DISPATCHER", createRole("DISPATCHER", Set.of(
            perms.get("BUILDING_READ"), perms.get("ROOM_READ"),
            perms.get("SENSOR_READ"), perms.get("READING_READ"), perms.get("READING_CREATE"),
            perms.get("ALERT_READ"), perms.get("ALERT_UPDATE"),
            perms.get("INCIDENT_READ"), perms.get("INCIDENT_UPDATE"),
            perms.get("REPORT_GENERATE")
        )));

        // TECHNICIAN - handle incidents
        roles.put("TECHNICIAN", createRole("TECHNICIAN", Set.of(
            perms.get("BUILDING_READ"), perms.get("ROOM_READ"),
            perms.get("SENSOR_READ"), perms.get("READING_READ"),
            perms.get("ALERT_READ"),
            perms.get("INCIDENT_READ"), perms.get("INCIDENT_UPDATE"),
            perms.get("FILE_UPLOAD")
        )));

        // VIEWER - read-only
        roles.put("VIEWER", createRole("VIEWER", Set.of(
            perms.get("BUILDING_READ"), perms.get("ROOM_READ"),
            perms.get("SENSOR_READ"), perms.get("READING_READ"),
            perms.get("ALERT_READ"), perms.get("INCIDENT_READ")
        )));

        log.info("Created {} roles", roles.size());
        return roles;
    }

    private Role createRole(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder().name(name).permissions(permissions).build();
            return roleRepository.save(role);
        });
    }

    private void createAdmin(Map<String, Role> roles) {
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .email("admin@firesafety.local")
                .enabled(true)
                .roles(Set.of(roles.get("ADMIN")))
                .build();
        userRepository.save(admin);

        User dispatcher = User.builder()
                .username("dispatcher")
                .password(passwordEncoder.encode("disp123"))
                .fullName("Fire Dispatcher")
                .email("dispatcher@firesafety.local")
                .enabled(true)
                .roles(Set.of(roles.get("DISPATCHER")))
                .build();
        userRepository.save(dispatcher);

        User technician = User.builder()
                .username("technician")
                .password(passwordEncoder.encode("tech123"))
                .fullName("Safety Technician")
                .email("technician@firesafety.local")
                .enabled(true)
                .roles(Set.of(roles.get("TECHNICIAN")))
                .build();
        userRepository.save(technician);

        log.info("Created admin, dispatcher, technician users");
    }

    private void createTestData() {
        Building building = buildingRepository.save(Building.builder()
                .name("Учебный корпус А")
                .address("ул. Университетская, д. 1")
                .description("Главный учебный корпус университета, 5 этажей")
                .build());

        Room room101 = roomRepository.save(Room.builder()
                .number("101")
                .floor(1)
                .purpose("Аудитория")
                .building(building)
                .build());

        Room room201 = roomRepository.save(Room.builder()
                .number("201")
                .floor(2)
                .purpose("Лаборатория")
                .building(building)
                .build());

        Room serverRoom = roomRepository.save(Room.builder()
                .number("003")
                .floor(0)
                .purpose("Серверная")
                .building(building)
                .build());

        sensorRepository.save(Sensor.builder()
                .inventoryNumber("SMOKE-101-01")
                .type(SensorType.SMOKE)
                .status(SensorStatus.ACTIVE)
                .room(room101)
                .thresholdValue(50.0)
                .build());

        sensorRepository.save(Sensor.builder()
                .inventoryNumber("TEMP-101-01")
                .type(SensorType.TEMPERATURE)
                .status(SensorStatus.ACTIVE)
                .room(room101)
                .thresholdValue(60.0)
                .build());

        sensorRepository.save(Sensor.builder()
                .inventoryNumber("GAS-201-01")
                .type(SensorType.GAS)
                .status(SensorStatus.ACTIVE)
                .room(room201)
                .thresholdValue(0.5)
                .build());

        sensorRepository.save(Sensor.builder()
                .inventoryNumber("SMOKE-SERVER-01")
                .type(SensorType.SMOKE)
                .status(SensorStatus.ACTIVE)
                .room(serverRoom)
                .thresholdValue(30.0)
                .build());

        sensorRepository.save(Sensor.builder()
                .inventoryNumber("BTN-101-01")
                .type(SensorType.MANUAL_BUTTON)
                .status(SensorStatus.ACTIVE)
                .room(room101)
                .thresholdValue(1.0)
                .build());

        log.info("Created test building, 3 rooms, 5 sensors");
    }
}
