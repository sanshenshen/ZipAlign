# ZipAlign

[中文说明](./README.zh-CN.md)

## What It Is For

ZipAlign is an Android app for checking and fixing APK alignment directly on the device.

It is useful when you want to:

- Verify whether an APK is properly aligned.
- Rebuild an APK with corrected ZIP entry alignment.
- Inspect APK files from device storage without moving them to a desktop first.
- Export the aligned APK back into the current folder.

## Main Features

- Full-storage file browsing on Android.
- File-manager style folder navigation.
- APK-like file recognition, including `.apk` and `.apk.1`.
- APK selection and inspection.
- Alignment verification similar to `zipalign`.
- APK rewrite and aligned export.
- Optional page alignment for uncompressed `.so` files.
- On-screen operation log and result summary.

## Typical Usage

1. Grant full file access.
2. Browse to the folder that contains the APK.
3. Select the APK file.
4. Run `Verify` to check alignment.
5. Run `Align and Export` to generate a corrected APK in the current folder.

## Current UI Highlights

- File-manager style browser.
- Folder enter and back animations.
- Custom Android-inspired APK icon.
- Stable browser panel that avoids abrupt layout jumps when folders contain only a few items.
