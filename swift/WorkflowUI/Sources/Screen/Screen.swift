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


/// Screens are the building blocks of an interactive application.
///
/// Conforming types contain any information needed to populate a screen: data,
/// styling, event handlers, etc.
public protocol Screen {
    /// Use `screenViewControllerDescription` to return the `ViewControllerDescription`
    var viewControllerDescription: ViewControllerDescription { get }
}

/// Temporary extension to enable soft-migration from `ViewRegistry`.
public extension Screen {
    var viewControllerDescription: ViewControllerDescription {
        fatalError("Return appropriate `ViewControllerDescription` from \(self)")
    }
}
