#import "SamsungHealthHandlerPlugin.h"
#if __has_include(<samsung_health_handler/samsung_health_handler-Swift.h>)
#import <samsung_health_handler/samsung_health_handler-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "samsung_health_handler-Swift.h"
#endif

@implementation SamsungHealthHandlerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSamsungHealthHandlerPlugin registerWithRegistrar:registrar];
}
@end
