import SwiftUI
import sharedKit

struct ContentView: View {
    private let roster = RosterCalculator.shared

    var body: some View {
        VStack(spacing: 16) {
            Text("FIFOKIT iOS")
                .font(.largeTitle)

            Text("2/1 roster: 14 days on, 7 days off")
                .font(.headline)

            ForEach([0, 13, 14, 20, 21], id: \.self) { day in
                let working = roster.isWorkDay(
                    daysFromStart: Int64(day),
                    workDays: 14,
                    offDays: 7
                )
                Text("Day \(day + 1): \(working ? "Work" : "Off")")
            }
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
