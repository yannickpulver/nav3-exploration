# Navigation3 Exploration

An Android app exploring the **Navigation3** library's advanced navigation patterns and adaptive UI strategies.

## Features

This project showcases more complex layouts that can be built with `Navigation 3`. 

### 🎯 **AdaptiveTwoPaneStrategy**
- Automatically switches between single-pane and two-pane layouts based on screen size
- Displays bottom navigation bar when needed (stretches over two pane if one of both panes requires it)
- Shows placeholder content on larger screens (if needed)
- Provides seamless tablet/phone experience

### 🔄 **OverlaySceneStrategy** 
- Displays overlay content as dialogs on tablets
- Shows content as bottom sheets on mobile devices
- Handles adaptive overlay presentation across different screen sizes

## Tech Stack

- **Navigation3** (alpha) - Next-generation Android navigation
- **Jetpack Compose** - Modern Android UI toolkit
- **Material3** - Latest Material Design components
- **Adaptive UI** - Responsive layouts for different screen sizes

## Demo

### Two-Pane Layout (Tablet)
![Two-Pane Demo](docs/twopane.gif)

### Single-Pane Layout (Mobile)
![Single-Pane Demo](docs/singlepane.gif)

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Run the app on different device sizes to see adaptive behavior
