//
// Created by Mikhail Nikonov on 30.12.25.
//

import Foundation

struct SwiftWriter {
    private(set) var lines: [String] = []
    private var indentLevel: Int = 0
    private let indentUnit = "    " // 4 spaces

    mutating func line(_ text: String = "") {
        let indent = String(repeating: indentUnit, count: indentLevel)
        lines.append(indent + text)
    }

    mutating func block(_ text: String) {
        let rawLines = text.split(separator: "\n", omittingEmptySubsequences: false)
        for l in rawLines {
            self.line(String(l))
        }
    }

    mutating func indent(_ body: (inout SwiftWriter) -> Void) {
        indentLevel += 1
        body(&self)
        indentLevel -= 1
    }

    func render() -> String {
        lines.joined(separator: "\n")
    }
}
