//
//  CaptchaScreen.swift
//  iosApp
//
//  Created by Mikhail Nikonov on 02.01.26.
//

import SharedSDK
import SwiftUI

struct CaptchaScreenView: View {
    @StateObject private var viewModel: ObservableCaptchaViewModel

    init(viewModel: CaptchaViewModel) {
        _viewModel = StateObject(
            wrappedValue: ObservableCaptchaViewModel(viewModel)
        )
    }
    
    var body: some View {
        ZStack {
            switch viewModel.uiState {
            case is CaptchaViewStateLoading:
                CaptchaLoadingView()

            case let active as CaptchaViewStateActive:
                CaptchaActiveView(
                    state: active,
                    onToggle: { image in
                        viewModel.onAction(
                            action: CaptchaViewActionToggleImage(image: image)
                        )
                    },
                    onSubmit: {
                        viewModel.onAction(action: CaptchaViewActionSubmit())
                    }
                )

            case is CaptchaViewStateFailed:
                CaptchaResultView(
                    tone: .negative,
                    title: "Verification failed",
                    description:
                        "Your selection didn’t match. Try again with a new challenge.",
                    primaryButton: "Try again",
                    onPrimary: {
                        viewModel.onAction(action: CaptchaViewActionRetry())
                    }
                )

            case is CaptchaViewStateSuccess:
                CaptchaResultView(
                    tone: .positive,
                    title: "Verified",
                    description: "Thanks! You passed the captcha.",
                    primaryButton: "Continue",
                    onPrimary: { /* navigate forward */  },
                    secondaryButton: "New challenge",
                    onSecondary: {
                        viewModel.onAction(action: CaptchaViewActionRetry())
                    }
                )

            default:
                // Fallback in case new states appear
                CaptchaLoadingView()
            }
        }
        .padding(16)
    }
}

// MARK: - Loading

private struct CaptchaLoadingView: View {
    var body: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text("Loading challenge…")
                .font(.body)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
    }
}

// MARK: - Active

private struct CaptchaActiveView: View {
    let state: CaptchaViewStateActive
    let onToggle: (CaptchaImage) -> Void
    let onSubmit: () -> Void

    private let columns: [GridItem] = Array(
        repeating: GridItem(.flexible(), spacing: 6),
        count: 3
    )

    var body: some View {
        VStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                Text("Captcha")
                    .font(.title2)
                    .fontWeight(.semibold)

                Text(state.promptText)
                    .font(.body)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            ScrollView {
                LazyVGrid(columns: columns, spacing: 10) {
                    ForEach(state.images, id: \.image.name) { item in
                        CaptchaImageTile(
                            model: item,
                            enabled: !state.isVerifying,
                            onTap: { onToggle(item.image) }
                        )
                    }
                }
                .padding(.top, 4)
            }

            Button(action: onSubmit) {
                HStack(spacing: 10) {
                    if state.isVerifying {
                        ProgressView()
                            .controlSize(.small)
                        Text("Verifying…")
                    } else {
                        Text("Verify")
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.borderedProminent)
            .disabled(state.isVerifying)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }
}

private struct CaptchaImageTile: View {
    let model: CaptchaImageUiModel
    let enabled: Bool
    let onTap: () -> Void

    private let corner: CGFloat = 14

    var body: some View {
        let borderColor: Color =
            model.isSelected ? .accentColor : Color(.separator).opacity(0.6)

        ZStack(alignment: .topTrailing) {
            Image(model.image.assetName)
                .resizable()
                .scaledToFill()  // center-crop
                .aspectRatio(contentMode: .fill)
                .frame(
                    minWidth: 0,
                    maxWidth: .infinity,
                    minHeight: 0,
                    maxHeight: .infinity
                )
                .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))  // rounded crop
                .aspectRatio(1, contentMode: .fit)
                .layoutPriority(-1)

            RoundedRectangle(cornerRadius: corner, style: .continuous)
                .stroke(borderColor, lineWidth: 2)
                .aspectRatio(1, contentMode: .fit)
            
            if model.isSelected {
                RoundedRectangle(cornerRadius: corner, style: .continuous)
                    .fill(Color.accentColor.opacity(0.18))

                Text("✓")
                    .font(.headline)
                    .foregroundStyle(Color(.systemBackground))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Capsule().fill(Color.accentColor))
                    .padding(8)
            }
        }
        .padding(2)
        .opacity(enabled ? 1.0 : 0.6)
        .onTapGesture {
            guard enabled else { return }
            onTap()
        }
    }
}

// MARK: - Result

private enum ResultTone { case positive, negative }

private struct CaptchaResultView: View {
    let tone: ResultTone
    let title: String
    let description: String
    let primaryButton: String
    let onPrimary: () -> Void
    let secondaryButton: String?
    let onSecondary: (() -> Void)?

    init(
        tone: ResultTone,
        title: String,
        description: String,
        primaryButton: String,
        onPrimary: @escaping () -> Void,
        secondaryButton: String? = nil,
        onSecondary: (() -> Void)? = nil
    ) {
        self.tone = tone
        self.title = title
        self.description = description
        self.primaryButton = primaryButton
        self.onPrimary = onPrimary
        self.secondaryButton = secondaryButton
        self.onSecondary = onSecondary
    }

    var body: some View {
        let accent: Color = (tone == .positive) ? .accentColor : .red

        VStack {
            Spacer()

            VStack(spacing: 10) {
                Text(tone == .positive ? "✓" : "!")
                    .font(.title)
                    .foregroundStyle(accent)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(
                        Capsule().fill(accent.opacity(0.12))
                    )

                Text(title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .multilineTextAlignment(.center)

                Text(description)
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 4)

                Button(action: onPrimary) {
                    Text(primaryButton)
                        .frame(maxWidth: .infinity, minHeight: 44)
                }
                .buttonStyle(.borderedProminent)

                if let secondaryButton, let onSecondary {
                    Button(action: onSecondary) {
                        Text(secondaryButton)
                            .padding(.vertical, 8)
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                    .buttonStyle(.bordered)
                }
            }
            .padding(18)
            .background(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .fill(Color(.secondarySystemBackground))
            )

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Mapping CaptchaImage -> iOS asset name

extension CaptchaImage {
    fileprivate var assetName: String {
        // Match your iOS asset catalog name.
        switch self {
        case .bus1: return "bus_1"
        case .bus2: return "bus_2"
        case .bus3: return "bus_3"
        case .bus4: return "bus_4"

        case .guitar1: return "guitar_1"
        case .guitar2: return "guitar_2"
        case .guitar3: return "guitar_3"
        case .guitar4: return "guitar_4"

        case .dog1: return "dog_1"
        case .dog2: return "dog_2"
        case .dog3: return "dog_3"
        case .dog4: return "dog_4"

        case .random1: return "random_1"
        case .random2: return "random_2"
        case .random3: return "random_3"
        case .random4: return "random_4"
        case .random5: return "random_5"
        case .random6: return "random_6"
        }
    }
}
