# Consumer ProGuard rules for forge-room module
# These rules will be automatically applied to apps that depend on this library

-keep class io.agora.board.forge.**{*;}

-keep class * extends io.agora.board.forge.ApplicationOption {
    <fields>;
}

