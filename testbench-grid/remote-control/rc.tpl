cpvars=lib/selenium-server-1.0.3.jar:lib/selenium-grid-remote-control-standalone-Vaadin-TestBench-@build@.jar

environment=linux-firefox3
userextensions=user-extensions.js

java -cp "$cpvars" com.thoughtworks.selenium.grid.remotecontrol.SelfRegisteringRemoteControlLauncher -env $environment -userExtensions $userextensions
