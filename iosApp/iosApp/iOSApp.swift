import SwiftUI
import SharedSDK

@main
struct iOSApp: App {
    init() {
        doInitDI(configuration: PlatformConfiguration())
    }
    
    var body: some Scene {
        WindowGroup {
            CaptchaScreenView(viewModel: CaptchaDependencies().getViewModel())
        }
    }
}
