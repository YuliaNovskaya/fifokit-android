# FIFOKIT iOS release checklist

Branch: `feature/kmp-ios`  
Xcode app: `iosApp/iosApp.xcodeproj`  
KMP framework: `sharedKit`

## Build verification on a Mac

1. Pull the branch and run shared tests:

   ```bash
   git pull origin feature/kmp-ios
   ./gradlew :shared:allTests
   ```

2. Build the iOS Simulator app in Xcode using the `iosApp` scheme.
3. Build a real-device target using `Any iOS Device (arm64)`; the Gradle task
   `:shared:embedAndSignAppleFrameworkForXcode` should select `iosArm64`.
4. For a Release build, choose `Product > Scheme > Edit Scheme > Run` and
   select Release, or use `Product > Archive` for an eligible destination.
5. Verify a Release build resolves `sharedKit` from
   `shared/build/xcode-frameworks/Release/$(SDK_NAME)`.

## Current project configuration

- Bundle identifier: `com.fifokit.iosApp`.
- Minimum iOS version: 17.2.
- Target devices: iPhone and iPad.
- User Script Sandboxing: disabled for `iosApp`.
- KMP framework built and signed by the Xcode run-script phase.
- The Compose iOS Info.plist performance flag is enabled.

## Before TestFlight distribution

- Add the official FIFOKIT app icon to `iosApp/iosApp/Assets.xcassets`.
  The current project contains no `AppIcon.appiconset`.
- In Xcode, choose your Apple Developer team under Signing & Capabilities.
  Keep developer accounts, certificates and private keys outside git.
- Confirm the bundle identifier is registered in App Store Connect.
- Increase the app's build number for each uploaded build.
- Test on a real supported iPhone or iPad, including keyboard input,
  date picker, navigation, offline roster persistence and restoration.
- Check iOS privacy disclosures for actual data collection and sharing.
- Verify Android and iOS user accounts, cloud roster sharing, subscriptions,
  reminders and PDF export separately; these are not fully migrated.
- Archive and validate in Xcode, then upload through Organizer.

This checklist is a preparation guide, not a claim that a device build,
Release archive, code signing or App Store upload has passed.
