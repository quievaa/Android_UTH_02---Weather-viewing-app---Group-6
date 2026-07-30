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

Đây là bản phân công đã chỉnh sửa để **cả 6 thành viên đều có phần lập trình riêng**, phù hợp cho báo cáo và bảo vệ đồ án.

---

## Member 1 – UI & Navigation Developer

**Role**
Phát triển giao diện người dùng và điều hướng ứng dụng.

**Responsibilities**

* Thiết kế giao diện theo Material Design.
* Xây dựng các màn hình chính của ứng dụng.
* Cài đặt Navigation giữa các màn hình.
* Thiết kế giao diện Responsive cho nhiều kích thước màn hình.
* Tích hợp giao diện với dữ liệu từ các module khác.
* Kiểm tra và chỉnh sửa giao diện sau khi tích hợp.

---

## Member 2 – API Integration Developer

**Role**
Phát triển chức năng kết nối với dịch vụ thời tiết.

**Responsibilities**

* Tích hợp OpenWeatherMap API.
* Thực hiện HTTP Request và nhận dữ liệu từ API.
* Quản lý API Key.
* Xử lý lỗi kết nối mạng.
* Kiểm tra dữ liệu trả về từ API.
* Hỗ trợ tích hợp API với các module khác.

---

## Member 3 – Weather Data Processing Developer

**Role**
Phát triển chức năng xử lý dữ liệu thời tiết.

**Responsibilities**

* Phân tích dữ liệu JSON.
* Xây dựng các lớp Model.
* Hiển thị thông tin thời tiết hiện tại.
* Xây dựng chức năng dự báo thời tiết nhiều ngày.
* Chuyển đổi dữ liệu để hiển thị lên giao diện.
* Tối ưu việc xử lý dữ liệu.

---

## Member 4 – Location & Search Developer

**Role**
Phát triển chức năng tìm kiếm và định vị.

**Responsibilities**

* Xây dựng chức năng tìm kiếm thời tiết theo tên thành phố.
* Lấy vị trí hiện tại bằng GPS.
* Hiển thị thời tiết theo vị trí hiện tại.
* Quản lý quyền truy cập Location.
* Cập nhật dữ liệu khi vị trí thay đổi.
* Kết nối chức năng tìm kiếm với API.

---

## Member 5 – Local Storage Developer

**Role**
Phát triển chức năng lưu trữ dữ liệu cục bộ.

**Responsibilities**

* Xây dựng chức năng lưu danh sách địa điểm yêu thích.
* Lưu lịch sử tìm kiếm.
* Quản lý SharedPreferences hoặc DataStore.
* Đồng bộ dữ liệu cục bộ với giao diện.
* Quản lý các thiết lập lưu trữ của ứng dụng.
* Kiểm tra việc lưu và đọc dữ liệu.

---

## Member 6 – Settings & Integration Developer

**Role**
Phát triển chức năng cài đặt và tích hợp hệ thống.

**Responsibilities**

* Xây dựng màn hình **Settings** của ứng dụng.
* Phát triển chức năng chuyển đổi đơn vị nhiệt độ (°C / °F).
* Xây dựng chức năng làm mới (Refresh) dữ liệu thời tiết.
* Xây dựng màn hình Splash Screen và xử lý trạng thái Loading/Error.
* Tích hợp các module thành một ứng dụng hoàn chỉnh.
* Kiểm thử, sửa lỗi và tối ưu hiệu năng của ứng dụng.

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
