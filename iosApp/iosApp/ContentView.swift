import SwiftUI
import sharedKit

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("FIFOKIT iOS")
                .font(.largeTitle)

            Text(Platform_iosKt.platform())
                .font(.headline)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
