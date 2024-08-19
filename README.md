# QuickQRScanner

QuickQRScanner is a simple Android application that allows users to scan QR codes using their device's camera. The app displays the scanned result in a text view and allows users to click on the result to open it in a web browser if it's a URL. This project uses Android's CameraX API and the ZXing library for QR code scanning.

## Features

- **Real-time Camera Preview**: Displays the camera feed directly in the app, allowing users to align and scan QR codes seamlessly.
- **QR Code Scanning**: Uses the ZXing library for accurate and fast QR code detection.
- **Clickable Result**: Scanned results are displayed and clickable. If the result is a URL, it opens in the browser; otherwise, it shows a toast message with the scanned data.
- **Modern UI**: A clean and user-friendly interface designed with a minimalistic approach.


## Getting Started

### Prerequisites

- Android Studio
- A device or emulator running Android 5.0 (Lollipop) or higher

### Installation

1. Clone the repository
2. Open the project in Android Studio.
3. Build and run the app on your device or emulator.

### Usage

- **Scanning**: Open the app, align a QR code within the frame displayed on the screen, and the app will automatically detect and display the result.
- **Opening URLs**: If the scanned result is a URL, tap on the displayed result to open it in your browser.

## Built With

- [CameraX](https://developer.android.com/training/camerax) - Used for handling camera preview and capturing images.
- [ZXing](https://github.com/zxing/zxing) - Used for QR code scanning.

## Contributing

Contributions are welcome! Please fork this repository and submit a pull request with your changes.


