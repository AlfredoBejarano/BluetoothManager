# BluetoothManager
Android app that harness the power of the Android Architecture Components to build a bluetooth device manager

![icon](https://github.com/AlfredoBejarano/BluetoothManager/blob/39fc8fda2f8cad942c3f8de564cdb5754f6ec085/app/src/main/ic_launcher-web.png?raw=true)

![screnshot](https://preview.ibb.co/i7qh8V/screenshot.png)

# Architecture

This app uses the Architecture Components from Google, as specified in the Android jetpack guidelines :robot::rocket:.

- Activity: The GameActivity class serves as the UI controller, receiving input events from the user and sending them to a ViewModel classes.
- ViewModel: The ViewModel class processes the UI event sent by the UI controller, (ie. consults the local database or process something), the result to the UI controller is reported via **Observation** using the **LiveData** component.
- Repository: The repository class serves as the single source of truth for the app (and the ViewModel), this class handles from where the data has to come from to be reported to the ViewModel (although in this app we only have a local database as a data source)

![arch](https://developer.android.com/topic/libraries/architecture/images/final-architecture.png)

# Dependencies

This app uses the following dependencies:

- Lifecycle: This dependency provides the ViewModel and LiveData classes.
- Room: Room is a persistence library that helps reducing boilerplate for local SQL operations.
- Architecture Test helpers: This library provides utility methods for unit testing ViewModel and Room dao classes.
- Dagger: Dagger makes dependency injection easy!, DI keeps the code testable and isolated.
- Espresso: Espresso is a powerful yet simple UI testing framework, this helps testing the UI of the app.
- Mockito: Mockito helps creating fake objects for unit testing environments!.
- Robolectric: This library helps mocking some of the core elements of the Android Framework (those doesn't exists in a JVM / Unit Test environment btw!).
