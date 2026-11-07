import SwiftUI
import sharedKit

struct RosterComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        RosterComposeViewControllerKt.rosterComposeViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    var body: some View {
        RosterComposeView()
    }
}

#Preview {
    ContentView()
}
