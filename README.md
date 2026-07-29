# Android_UTH_02 – Weather Viewing App

## 1. Project Overview

**Android_UTH_02 – Weather Viewing App** là ứng dụng di động được phát triển trên nền tảng Android nhằm hỗ trợ người dùng tra cứu thông tin thời tiết theo thời gian thực tại các địa điểm trên toàn thế giới.

Ứng dụng cho phép người dùng tìm kiếm thành phố, xem thời tiết hiện tại, theo dõi dự báo thời tiết trong nhiều ngày tiếp theo và lưu các địa điểm yêu thích để truy cập nhanh. Ngoài ra, ứng dụng còn hỗ trợ lấy vị trí hiện tại của thiết bị để hiển thị thông tin thời tiết tương ứng mà không cần nhập tên địa điểm.

Dữ liệu thời tiết được lấy từ **OpenWeatherMap API** thông qua kết nối Internet và được xử lý để hiển thị dưới dạng trực quan, dễ đọc và thân thiện với người dùng.

Dự án được thực hiện trong học phần **Mobile Device Programming**, với mục tiêu giúp sinh viên vận dụng kiến thức về phát triển ứng dụng Android bằng **Kotlin**, thiết kế giao diện hiện đại, làm việc với REST API và quản lý dữ liệu trên thiết bị di động.

---

# 2. Project Information

**Project Code:** Android_UTH_02

**Project Name:** Weather Viewing App

**Project Type:** Android Mobile Application

**Course:** Mobile Device Programming

**Programming Language:** Kotlin

**Development Environment:** Android Studio

**Architecture:** MVVM (Model – View – ViewModel)

**API Service:** OpenWeatherMap API

**Minimum SDK:** Android 8.0 (API 26)

**Target SDK:** Android 15

---

# 3. Team Members & Responsibilities

Để đảm bảo tiến độ phát triển dự án và nâng cao hiệu quả làm việc nhóm, các thành viên được phân công thực hiện các nhiệm vụ sau:

## Member 1 – Project Management & UI Development

### Role

Quản lý tiến độ dự án và phát triển giao diện người dùng.

### Responsibilities

* Lập kế hoạch và theo dõi tiến độ dự án.
* Thiết kế giao diện theo Material Design.
* Xây dựng các màn hình chính của ứng dụng.
* Điều hướng giữa các màn hình.
* Kiểm tra tính nhất quán của giao diện.
* Hỗ trợ tích hợp các chức năng.

---

## Member 2 – API Integration Engineer

### Role

Phát triển chức năng kết nối và giao tiếp với dịch vụ thời tiết.

### Responsibilities

* Tích hợp OpenWeatherMap API.
* Thực hiện HTTP Request và nhận dữ liệu từ API.
* Quản lý API Key.
* Kiểm tra lỗi kết nối mạng.
* Đảm bảo dữ liệu được cập nhật chính xác.

---

## Member 3 – Weather Data Processing Engineer

### Role

Xử lý dữ liệu thời tiết và hiển thị thông tin.

### Responsibilities

* Phân tích dữ liệu JSON.
* Xây dựng các lớp Model.
* Hiển thị thông tin thời tiết hiện tại.
* Xây dựng chức năng dự báo nhiều ngày.
* Cập nhật dữ liệu lên giao diện.

---

## Member 4 – Location & Search Engineer

### Role

Phát triển chức năng tìm kiếm và định vị.

### Responsibilities

* Tìm kiếm thời tiết theo tên thành phố.
* Lấy vị trí hiện tại bằng GPS.
* Hiển thị thời tiết theo vị trí.
* Quản lý quyền truy cập Location.
* Cập nhật dữ liệu khi vị trí thay đổi.

---

## Member 5 – Local Data Engineer

### Role

Quản lý dữ liệu được lưu trên thiết bị.

### Responsibilities

* Lưu danh sách địa điểm yêu thích.
* Lưu lịch sử tìm kiếm.
* Quản lý SharedPreferences hoặc DataStore.
* Đồng bộ dữ liệu cục bộ.
* Quản lý các thiết lập của ứng dụng.

---

## Member 6 – Testing & Quality Assurance Engineer

### Role

Kiểm thử và hoàn thiện hệ thống.

### Responsibilities

* Kiểm thử các chức năng của ứng dụng.
* Tích hợp các module.
* Phát hiện và sửa lỗi.
* Tối ưu hiệu năng.
* Chuẩn bị tài liệu và video demo.

---

# 4. Main Features

###  Current Weather

* Hiển thị nhiệt độ hiện tại.
* Hiển thị độ ẩm.
* Hiển thị áp suất không khí.
* Hiển thị tốc độ gió.
* Hiển thị cảm giác thực tế.
* Hiển thị biểu tượng thời tiết.

---

###  Search Weather

* Tìm kiếm theo tên thành phố.
* Hiển thị kết quả nhanh.
* Hỗ trợ nhiều quốc gia.

---

###  Current Location

* Lấy vị trí hiện tại của người dùng.
* Hiển thị thời tiết theo GPS.
* Tự động cập nhật khi vị trí thay đổi.

---

###  Favorite Locations

* Thêm địa điểm yêu thích.
* Xóa địa điểm.
* Truy cập nhanh thông tin thời tiết.

---

###  Weather Forecast

* Hiển thị dự báo thời tiết trong 5 ngày.
* Hiển thị nhiệt độ cao nhất và thấp nhất.
* Hiển thị điều kiện thời tiết.
* Hiển thị biểu tượng thời tiết.

---

### ⚙ Settings

* Chuyển đổi đơn vị nhiệt độ.
* Quản lý cài đặt ứng dụng.
* Đồng bộ dữ liệu người dùng.

---

# 5. Application Architecture

Ứng dụng được xây dựng theo mô hình **MVVM**, giúp tách biệt giao diện, dữ liệu và logic xử lý nhằm tăng khả năng bảo trì và mở rộng.

```text
User Interface
      │
      ▼
 ViewModel
      │
      ▼
 Repository
      │
 ┌────┴────┐
 ▼         ▼
Weather API Local Storage
```

---

# 6. User Interface

Ứng dụng bao gồm các màn hình chính:

### Splash Screen

* Khởi tạo ứng dụng.

### Home Screen

* Hiển thị thời tiết hiện tại.

### Search Screen

* Tìm kiếm thành phố.

### Forecast Screen

* Hiển thị dự báo thời tiết.

### Favorite Screen

* Danh sách địa điểm yêu thích.

### Settings Screen

* Quản lý cài đặt.

---

# 7. Technologies Used

* Kotlin
* Android Studio
* Android SDK
* MVVM Architecture
* Retrofit
* Coroutines
* ViewModel
* LiveData
* Navigation Component
* Material Design 3
* RecyclerView
* Glide
* Gson
* OpenWeatherMap API
* Google Play Services Location
* SharedPreferences
* Git
* GitHub

---

# 8. Project Structure

```text
Android_UTH_02
│
├── app
├── data
│   ├── api
│   ├── model
│   └── repository
│
├── ui
│   ├── home
│   ├── search
│   ├── forecast
│   ├── favorite
│   └── settings
│
├── viewmodel
├── utils
├── assets
├── res
└── README.md
```

---

# 9. Project Objectives

Mục tiêu của dự án:

* Phát triển ứng dụng Android bằng Kotlin.
* Áp dụng mô hình MVVM.
* Kết nối RESTful API.
* Xử lý dữ liệu JSON.
* Thiết kế giao diện Material Design.
* Sử dụng GPS để xác định vị trí.
* Quản lý dữ liệu cục bộ.
* Thực hành làm việc nhóm bằng GitHub.

---

# 10. Development Status

## Completed

* Thiết kế giao diện.
* Kết nối API.
* Hiển thị thời tiết hiện tại.
* Chức năng tìm kiếm.
* Chức năng GPS.
* Dự báo thời tiết.
* Lưu địa điểm yêu thích.

### Future Improvements

* Thông báo thời tiết.
* AQI (Air Quality Index).
* UV Index.
* Widget thời tiết.
* Hỗ trợ nhiều ngôn ngữ.
* Đồng bộ Firebase.

---

# 11. Installation Guide

### Requirements

* Android Studio Hedgehog hoặc mới hơn.
* Android SDK 26 trở lên.
* Internet Connection.
* OpenWeatherMap API Key.

### Steps

1. Clone repository.
2. Mở project bằng Android Studio.
3. Đồng bộ Gradle.
4. Cấu hình API Key.
5. Build project.
6. Chạy trên Emulator hoặc thiết bị Android.

---

# 12. Course Information

**Course:** Mobile Device Programming

**Project Type:** Android Group Project

**Project Code:** Android_UTH_02

**Academic Year:** 2025–2026

---

# 13. Project Timeline

 **Week 1**

* Khởi tạo repository GitHub.
* Phân chia nhiệm vụ.
* Thiết kế giao diện sơ bộ.

 **Week 2**

* Xây dựng cấu trúc dự án.
* Thiết lập mô hình MVVM.
* Kết nối OpenWeatherMap API.

 **Week 3**

* Hoàn thiện chức năng tìm kiếm.
* Hiển thị thời tiết hiện tại.
* Tích hợp GPS.

 **Week 4**

* Hoàn thiện dự báo thời tiết.
* Quản lý địa điểm yêu thích.
* Lưu dữ liệu cục bộ.

 **Week 5**

* Kiểm thử.
* Tối ưu hiệu năng.
* Hoàn thiện giao diện.

 **Week 6**

* Hoàn thiện tài liệu.
* Chuẩn bị slide.
* Quay video demo.
* Bảo vệ đồ án.
