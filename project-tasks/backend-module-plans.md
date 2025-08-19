# Szczegółowe plany implementacji backendu FlickIt

## 1. User (Użytkownik)

### Opis
Moduł zarządza użytkownikami systemu: rejestracja, pobieranie danych, autoryzacja, role, oceny.

### Kluczowe klasy i pliki:
- Model: `UserEntity.java`
- Repozytorium: `UserRepository.java`
- DTO: `UserDto.java`, `CreateUserRequest.java`, `UserLoginRequest.java`
- Serwis: `UserService.java`
- Kontroler: `UserController.java`

### Przepływy i endpointy
- **POST /users** — rejestracja użytkownika
  - Walidacja: unikalny numer telefonu, wymagane pola (name, phone, role)
  - Tworzenie encji, domyślne wartości rating/ratingCount
  - Zwraca: UserDto
- **GET /users/{id}** — pobranie użytkownika po ID (ADMIN)
  - Sprawdzenie uprawnień (ADMIN)
  - Zwraca: UserDto lub 404
- **GET /users/me** — pobranie własnego profilu (CUSTOMER, VENDOR, ADMIN)
  - Pobranie ID z kontekstu JWT
  - Zwraca: UserDto lub 401

### Walidacje
- Unikalność numeru telefonu
- Role: VENDOR, CUSTOMER, ADMIN
- Hasło przechowywane jako hash (BCrypt)

### Zależności
- `UserService` korzysta z `UserRepository`, `ObjectMapper`, `BCryptPasswordEncoder`
- `UserController` korzysta z `UserService`, `AuthContext`

---

## 2. Auth (Autoryzacja)

### Opis
Obsługa logowania, generowania tokenów JWT, weryfikacji uprawnień.

### Kluczowe klasy i pliki:
- Serwis: `JwtService.java`
- Kontrolery: `AuthController.java`, `TokenController.java`
- DTO: `UserLoginRequest.java`
- Repozytorium: `UserRepository.java`

### Przepływy i endpointy
- **POST /auth/login** — logowanie użytkownika
  - Pobranie użytkownika po numerze telefonu
  - Weryfikacja hasła (BCrypt)
  - Generowanie tokena JWT (rola, userId)
  - Zwraca: { token }
- **POST /auth/token** — generowanie tokena JWT (admin/test)
  - Parametry: userId, role
  - Zwraca: token JWT
- **POST /auth/test-token** — generowanie testowego tokena (TokenController)

### Walidacje
- Poprawność hasła
- Istnienie użytkownika

### Zależności
- `JwtService` — generowanie i weryfikacja JWT
- `UserService`, `UserRepository`

---

## 3. Event (Wydarzenia)

### Opis
Zarządzanie wydarzeniami: tworzenie, pobieranie, lista, kategorie, statusy.

### Kluczowe klasy i pliki:
- Model: `EventEntity.java`
- Repozytorium: `EventRepository.java`
- DTO: `EventDto.java`
- Serwis: `EventService.java`
- Kontroler: `EventController.java`

### Przepływy i endpointy
- **GET /events** — lista wszystkich wydarzeń
  - Zwraca: List<EventDto>
- **GET /events/{id}** — szczegóły wydarzenia
  - Zwraca: EventDto lub 404
- **POST /events** — tworzenie wydarzenia (VENDOR)
  - Walidacja uprawnień (VENDOR)
  - Tworzenie encji na podstawie DTO
  - Zwraca: EventDto

### Walidacje
- Wymagane pola: title, description, lokalizacja, kategoria, expiresAt
- Status: ACTIVE, CLAIMED, EXPIRED, REMOVED

### Zależności
- `EventService` korzysta z `EventRepository`, `ObjectMapper`
- `EventController` korzysta z `EventService`

---

## 4. Claim (Roszczenia)

### Opis
Obsługa zgłaszania roszczeń do wydarzeń oraz oceniania zrealizowanych eventów.

### Kluczowe klasy i pliki:
- Model: `ClaimEntity.java`, `ClaimId.java`
- Repozytorium: `ClaimRepository.java`
- DTO: `ClaimEventRequest.java`, `RateEventRequest.java`
- Serwis: `ClaimService.java`
- Kontroler: `ClaimController.java`

### Przepływy i endpointy
- **POST /claims** — zgłoszenie roszczenia (CUSTOMER)
  - Walidacja: czy już zgłoszono, czy event istnieje
  - Tworzenie encji ClaimEntity
  - Zwraca: 200 lub 409
- **POST /claims/rate** — ocena zrealizowanego eventu (CUSTOMER)
  - Walidacja: czy roszczenie istnieje, czy nie było ocenione
  - Aktualizacja encji ClaimEntity (rating, comment, ratedAt)
  - Zwraca: 200 lub 400

### Walidacje
- Unikalność roszczenia (eventId+userId)
- Czy event istnieje
- Czy nie oceniono już wcześniej

### Zależności
- `ClaimService` korzysta z `ClaimRepository`
- `ClaimController` korzysta z `ClaimService`

---

## 5. Rating (Oceny)

### Opis
Obsługa oceniania eventów przez użytkowników, aktualizacja ratingu vendorów.

### Kluczowe klasy i pliki:
- Model: `RatingEntity.java`
- Repozytorium: `RatingRepository.java`
- DTO: `CreateRatingRequest.java`, `RatingDto.java`
- Serwis: `RatingService.java`
- Kontroler: `RatingController.java`

### Przepływy i endpointy
- **POST /ratings** — wystawienie oceny (CUSTOMER)
  - Walidacja: czy użytkownik już oceniał event
  - Tworzenie encji RatingEntity
  - Aktualizacja ratingu vendora
  - Zwraca: RatingDto

### Walidacje
- Zakres oceny: 1-5
- Komentarz opcjonalny
- Unikalność oceny (eventId+userId)

### Zależności
- `RatingService` korzysta z `RatingRepository`, `UserRepository`, `EventRepository`, `ObjectMapper`, `AuthContext`
- `RatingController` korzysta z `RatingService`

---

## 6. Config/Security

### Opis
Konfiguracja bezpieczeństwa, filtry JWT, uprawnienia endpointów.

### Kluczowe klasy i pliki:
- Konfiguracja: `SecurityConfig.java`
- Filtr: `JwtAuthenticationFilter.java`
- Model: `CurrentUser.java`
- Serwis: `AuthContext.java`

### Przepływy
- Filtr JWT sprawdza token w każdym żądaniu (poza /auth/**)
- Ustawienie CurrentUser w kontekście
- Konfiguracja uprawnień endpointów (Spring Security)

### Zależności
- `SecurityConfig` korzysta z `JwtAuthenticationFilter`
- `JwtAuthenticationFilter` korzysta z `JwtService`, `UserRepository`, `AuthContext`

---

## 7. Notification, AI (future)

Moduły zarezerwowane na przyszłość, obecnie brak implementacji. 