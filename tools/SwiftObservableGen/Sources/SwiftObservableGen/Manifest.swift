//
// Created by Mikhail Nikonov on 29.12.25.
//

import Foundation

struct Manifest: Codable {
    let version: Int?
    let viewModels: [ViewModelEntry]
}

struct ViewModelEntry: Codable {
    let className: String
    let simpleName: String

    let state: StateEntry?
    let actions: [ActionEntry]
}

struct StateEntry: Codable {
    let property: String
    let typeSimpleName: String
}

struct ActionEntry: Codable {
    let function: String
    let params: [ActionParamsEntry]
}

struct ActionParamsEntry: Codable {
    let paramLabel: String
    let typeSimpleName: String
}
