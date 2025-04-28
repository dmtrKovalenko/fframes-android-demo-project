# fframes + android

This is showing how you can use fframes cross compiled for andorid using native development kit.

## Requirements

- Android Studio
- Android NDK toolchain (installed with Android Studio)
- Rust android toolchain

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

- [cargo-ndk](https://github.com/bbqsrc/cargo-ndk)

This is a CLI allows to resolve android NDK toolchain used and cross compile jni libraries for all the platforms

```
cargo install cargo-ndk
```

## Build

This **should** work out of the box if you open android studio and press the cargo build, the gradle scripts have to automatically run the rust build and copy the libraries to the right place. To run the compilation on your use the following command:

```bash
cd hello_rust_lib
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86 -t x86_64 -o ../android-app/app/src/main/jniLibs build --release
```

To introspect the build progress add the `--vv` flag to the cargo ndk command

## Configure android platform

To configure the way android ndk is building rust code there are several environment varialbes supported by the cargo ndk for example `ANDROID_PLATOFRM`. For more information see [the cargo-ndk docs](https://github.com/bbqsrc/cargo-ndk)

## Output

Make sure that android doesn't support stdout so you would have to implement your custom logger for example with a help of [android_logger crate](https://crates.io/crates/android_logger)
