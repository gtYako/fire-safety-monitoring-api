@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Maven Start Up Batch script

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE (SET "BASE_DIR=%__MVNW_ARG0_NAME__%")
@IF "%BASE_DIR:~-1%"=="\" SET "BASE_DIR=%BASE_DIR:~0,-1%"
@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
@SET DOWNLOAD_URL=

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@SET MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper
@SET MAVEN_WRAPPER_DOWNLOADER=%MAVEN_USER_HOME%\dists

@IF NOT EXIST "%MAVEN_WRAPPER_JAR%" (
    IF NOT EXIST "%MAVEN_USER_HOME%" MD "%MAVEN_USER_HOME%"
    powershell -Command "& { Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%MAVEN_WRAPPER_JAR%' }"
)

@"%JAVA_HOME%\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
