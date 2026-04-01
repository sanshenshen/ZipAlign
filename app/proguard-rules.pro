# Keep the launcher entry point explicit.
-keep class com.lingxing.zipalign.app.MainActivity

# This project does not rely on reflection-based model binding or serialization.
# Let R8 fully shrink and obfuscate internal feature/core classes.
