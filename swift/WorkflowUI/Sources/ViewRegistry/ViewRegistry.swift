/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#if canImport(UIKit)

import UIKit
import ReactiveSwift


/// Maps screen models into live views.
///
/// In order for the registry to handle a given screen type, a view factory
/// must first be registered using `register(screenType:factory:)`, where the
/// factory is a simple closure that is responsible for instantiating a view.
///
/// Deprecated: Return the appropriate `ViewControllerDescription` from `Screen` instead
public struct ViewRegistry {

    /// Defines a closure that instantiates a live view instance.
    private typealias Factory<T: Screen> = (T, ViewRegistry) -> ScreenViewController<T>

    private var factories: [ObjectIdentifier:Any] = [:]

    /// Initializes an empty registry.
    public init() {
        // `AnyScreen` is a WorkflowUI primitive; all view registries should support them.
        register(screenViewControllerType: AnyScreenViewController.self)
    }

    /// Convenience registration method that wraps a simple `UIViewController` in a `ScreenViewController` to provide convenient
    /// update methods.
    @available(*, deprecated, message:"Return the appropriate `ViewControllerDescription` from `Screen` instead.")
    public mutating func register<ViewControllerType, ScreenType>(screenViewControllerType: ViewControllerType.Type) where ViewControllerType: ScreenViewController<ScreenType> {

        let factory: Factory<ScreenType> = { screen, registry -> ScreenViewController<ScreenType> in
            return ViewControllerType(screen: screen, viewRegistry: registry)
        }
        factories[ObjectIdentifier(ScreenType.self)] = factory

    }

    /// Returns `true` is a factory block has previously been registered for the screen type `T`.
    @available(*, deprecated, message:"returns incorrect value if `ViewControllerDescription` is used.")
    public func canProvideView<T>(for screenType: T.Type) -> Bool where T : Screen {
        return factories[ObjectIdentifier(screenType)] != nil
    }

    /// Instantiates and returns a view instance for the given screen model.
    ///
    /// Note that you must check `canProvideView(for:)` before calling this method. Calling `provideView(for:)`
    /// with a screen type that was not previously registered is a programmer error, and the application will crash.
    public func provideView<T>(for screen: T) -> ScreenViewController<T> where T : Screen {
        guard let factory = factories[ObjectIdentifier(T.self)] as? Factory<T> else {
            if let vc = screen.viewControllerDescription.build() as? ScreenViewController<T> {
                vc.viewRegistry = self
                return vc
            }
            fatalError("The screen type \(T.self) was not registered with the view registry.")
        }
        return factory(screen, self)
    }

    /// Merges from another registry. If a screen type is registered with both,
    /// the definition from the other registry will replace the original in `self`.
    public mutating func merge(with otherRegistry: ViewRegistry) {
        factories.merge(otherRegistry.factories) { (_, new) -> Any in
            return new
        }
    }

    /// The returned value is identical to the result of calling `merge(from:)` on `self`.
    public func merged(with otherRegistry: ViewRegistry) -> ViewRegistry {
        var result = self
        result.merge(with: otherRegistry)
        return result
    }

}

#endif
