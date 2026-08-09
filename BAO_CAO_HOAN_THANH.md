# HOÀN THÀNH YÊU CẦU – WEATHER VIEWING APP

## 1. Phân tích cấu trúc dữ liệu JSON

Ứng dụng sử dụng Open-Meteo Forecast API (Current Weather) với endpoint `weather`.

Các nhóm dữ liệu chính:

```text
{
  "coord": {
    "lon": 106.63,
    "lat": 10.82
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "trời quang",
      "icon": "01d"
    }
  ],
  "main": {
    "temp": 31.2,
    "feels_like": 34.1,
    "temp_min": 29.8,
    "temp_max": 32.4,
    "pressure": 1009,
    "humidity": 72
  },
  "wind": {
    "speed": 3.2,
    "deg": 120
  },
  "dt": 1750000000,
  "name": "Ho Chi Minh City",
  "id": 1566083
}
```

Cấu trúc JSON được chia thành:
- `coord`: tọa độ địa lý.
- `weather`: danh sách mô tả thời tiết; ứng dụng lấy phần tử đầu tiên.
- `main`: nhiệt độ, cảm giác, nhiệt độ thấp/cao, áp suất và độ ẩm.
- `wind`: tốc độ và hướng gió; có thể không tồn tại nên Model dùng nullable.
- `name`: tên thành phố.
- `id`, `dt`: thông tin định danh/thời điểm.

Trong source hiện có, Gson DTO đã ánh xạ các trường JSON bằng `@SerializedName`.

## 2. Thiết kế các lớp Model

### Remote DTO
- `WeatherResponse`: đối tượng JSON cấp cao.
- `MainDto`: nhóm dữ liệu nhiệt độ/độ ẩm/áp suất.
- `WeatherDto`: mô tả điều kiện thời tiết.
- `WindDto`: dữ liệu gió.
- `CoordDto`: tọa độ.
- Các DTO forecast giữ nguyên để phục vụ phần dự báo sau này.

### Domain Model
Đã bổ sung:

`domain/model/CurrentWeather.kt`

Model này chỉ chứa dữ liệu cần cho nghiệp vụ/giao diện và không phụ thuộc JSON.

Các trường:
- cityName
- temperatureC
- feelsLikeC
- minTemperatureC
- maxTemperatureC
- description
- weatherMain
- humidityPercent
- pressureHpa
- windSpeedMps
- windDirectionDeg
- latitude
- longitude
- iconCode

## 3. Xác định trường dữ liệu hiển thị

Màn hình Current Weather hiển thị:
1. Tên thành phố.
2. Điều kiện thời tiết.
3. Nhiệt độ hiện tại.
4. Cảm giác như.
5. Độ ẩm.
6. Tốc độ gió.
7. Áp suất.
8. Nhiệt độ thấp nhất.
9. Nhiệt độ cao nhất.
10. Tọa độ.
11. Hướng gió nếu API trả về.

## 4. Luồng xử lý dữ liệu

```text
UI (HomeScreen)
      ↓
WeatherViewModel
      ↓
WeatherRepository
      ↓
WeatherApiService (Retrofit)
      ↓
Open-Meteo API
      ↓
JSON
      ↓
Gson
      ↓
WeatherResponse / DTO
      ↓
Mapper to CurrentWeather
      ↓
WeatherUiState
      ↓
Compose UI
```

Vai trò:
- API Service: gọi HTTP.
- Repository: xử lý Response và chuyển DTO thành Domain Model.
- ViewModel: quản lý coroutine và trạng thái UI.
- UI: quan sát StateFlow và hiển thị Loading/Success/Error.

## 5. Chuẩn bị Repository và ViewModel

### Repository
`data/repository/WeatherRepository.kt`

Repository:
- Gọi `getCurrentWeather()`.
- Kiểm tra HTTP status.
- Xử lý response rỗng.
- Bắt exception mạng.
- Mapping `WeatherResponse -> CurrentWeather`.
- Trả về `Result<CurrentWeather>`.

### ViewModel
`ui/viewmodel/WeatherViewModel.kt`

ViewModel cung cấp:

```text
Loading
Success(CurrentWeather)
Error(message)
```

`StateFlow` được dùng để UI tự động cập nhật khi dữ liệu thay đổi.

## 6. Xây dựng Model cho Current Weather

Đã hoàn thành trong:

`domain/model/CurrentWeather.kt`

Điểm quan trọng là Domain Model không chứa `@SerializedName`, vì nó không còn phụ thuộc cấu trúc JSON.

## 7. Phân tích dữ liệu JSON trả về

Gson đọc JSON và tự động ánh xạ:
- `name` → `WeatherResponse.cityName`
- `main.temp` → `MainDto.temp`
- `main.feels_like` → `MainDto.feelsLike`
- `main.humidity` → `MainDto.humidity`
- `weather[0].description` → `WeatherDto.description`
- `wind.speed` → `WindDto.speed`
- `coord.lat/lon` → `CoordDto.lat/lon`

Các trường có khả năng không xuất hiện như `coord`, `wind` được khai báo nullable.

## 8. Chuyển đổi API sang Model

Mapping nằm trong Repository:

```text
WeatherResponse
    ↓
weather.firstOrNull()
    ↓
CurrentWeather
```

Việc mapping tại Repository giúp UI không cần biết JSON có cấu trúc như thế nào.

## 9. Hiển thị Current Weather

`HomeScreen.kt` đã được chuyển từ dữ liệu hard-code sang dữ liệu thực từ ViewModel.

Ví dụ:
- Không còn cố định `"Ho Chi Minh City"`.
- Không còn cố định `"31 C"`.
- Không còn cố định `"72%"`.
- Các giá trị lấy trực tiếp từ `CurrentWeather`.

Khi tìm thành phố trong Search, tên thành phố được gửi về ViewModel, gọi API và quay lại Home để hiển thị kết quả.

## 10. Xử lý Loading / Success / Error

### Loading
Hiển thị `CircularProgressIndicator` và thông báo đang tải.

### Success
Hiển thị đầy đủ dữ liệu thời tiết.

### Error
Hiển thị:
- Tiêu đề lỗi.
- Nội dung lỗi.
- Nút `Thử lại`.

Một số HTTP error được xử lý riêng:
- Lỗi tìm kiếm thành phố: hiển thị thông báo không tìm thấy thành phố.
- Lỗi API: hiển thị mã HTTP.
- Lỗi mạng: hiển thị thông báo kết nối và cho phép thử lại.

## 11. Cấu hình API

Project đã chuyển sang Open-Meteo nên **không cần API key** và không cần cấu hình `BuildConfig` cho khóa API. Chỉ cần thiết bị/emulator có quyền Internet.

Ứng dụng sử dụng hai endpoint:
- Open-Meteo Forecast API để lấy Current Weather.
- Open-Meteo Geocoding API để chuyển tên thành phố thành latitude/longitude.

## 12. Kết quả kiến trúc

Project hiện đi theo mô hình gần MVVM:

```text
data/
 ├── remote/
 │   ├── api/
 │   └── model/
 └── repository/

domain/
 └── model/

ui/
 ├── components/
 ├── model/
 ├── navigation/
 ├── screens/
 └── viewmodel/
```

Kiến trúc này tách biệt:
- dữ liệu API,
- nghiệp vụ,
- trạng thái,
- giao diện.

Do đó dễ kiểm thử, bảo trì và mở rộng sang Forecast/Favorite sau này.

## 13. Checklist yêu cầu

| Yêu cầu | Trạng thái |
|---|---|
| Phân tích cấu trúc JSON | Đã hoàn thành |
| Thiết kế lớp Model | Đã hoàn thành |
| Xác định trường hiển thị | Đã hoàn thành |
| Thiết kế luồng xử lý | Đã hoàn thành |
| Repository | Đã hoàn thành |
| ViewModel | Đã hoàn thành |
| Model Current Weather | Đã hoàn thành |
| Phân tích JSON trả về | Đã hoàn thành |
| API → Model | Đã hoàn thành |
| Hiển thị Current Weather | Đã hoàn thành |
| Loading | Đã hoàn thành |
| Success | Đã hoàn thành |
| Error + Retry | Đã hoàn thành |
