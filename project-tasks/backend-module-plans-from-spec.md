# Szczegółowe plany backendu FlickIt (na bazie specyfikacji)

---

## 1. Event (Deals/Events)

**Odpowiedzialność:**
- Zarządzanie cyklem życia eventów (tworzenie, pobieranie, aktualizacja, usuwanie)
- Walidacja pól, obsługa zdjęć, integracja z AI (generowanie tytułu/opisu)
- Kategoryzacja, compliance, statusy

**Główne DTO:**
- EventDto (id, titleAi, titleVendor?, descriptionAi, descriptionVendor?, lat, lon, alt?, floor?, images, aiImageUrl?, visionLabels?, category, expiresAt, vendorId)
- CreateEventRequest (imagesBase64, aiImageUrl, discount, style, expiresAt, category, expiryDate?, productCategory, foodExpiryType?, foodExpiryDate?)
- VisionLabelDto (label, confidence)

**Encje:**
- Event (id, status, titleAi, titleVendor?, descriptionAi, descriptionVendor?, lat, lon, alt, floor?, expiresAt, vendorId, ...)
- EventImage (id, eventId, url, idx, isAiSource?)
- EventVisionLabel (eventImageId, label, confidence)
- EventCompliance (eventId, category, expiryDate?)

**Endpointy:**
- POST /events — tworzenie eventu (vendor)
- GET /events/latest?geo=… — pobieranie eventów po geolokalizacji
- PUT /events/{id} — aktualizacja eventu (vendor/admin)
- DELETE /events/{id} — usuwanie eventu (zmiana statusu na REMOVED)

**Walidacje:**
- Maks. 3 zdjęcia, poprawność base64
- Wymagane pola: kategoria, expiresAt, discount, style
- Compliance: dla FOOD wymagane expiryDate, foodExpiryType, foodExpiryDate
- Uprawnienia: tylko vendor/admin może edytować/usuwać

**Przepływy:**
- Vendor tworzy event → AI generuje tytuł/opis → event zapisany w DB → push do subskrybentów w promieniu 2km
- Klient pobiera eventy po lokalizacji

**Testy:**
- Jednostkowe i integracyjne dla CRUD, walidacji, compliance, uprawnień

---

## 2. Claim (Claimowanie eventu)

**Odpowiedzialność:**
- Pozwala klientowi zarezerwować (claimować) event
- Walidacja statusu eventu, unikalności claimu

**DTO:**
- ClaimEventRequest (eventId, userId)

**Encje:**
- Claim (eventId, userId, createdAt, rating?, comment?, ratedAt?)

**Endpointy:**
- PUT /events/{id}/claim — claimowanie eventu przez klienta

**Walidacje:**
- Event musi być aktywny
- Jeden claim na userId+eventId

**Przepływy:**
- Klient claimuje event → DB zapisuje claim → event zmienia status na CLAIMED (opcjonalnie)

**Testy:**
- Jednostkowe i integracyjne claimowania, obsługa błędów

---

## 3. Rating (Oceny eventów)

**Odpowiedzialność:**
- Pozwala klientowi ocenić event po zrealizowaniu (geo-fence)
- Walidacja ratingu, komentarza, geo-fence

**DTO:**
- RateEventRequest (eventId, rating, comment?, ratedAt?)

**Encje:**
- Claim (eventId, userId, rating, comment, ratedAt)

**Endpointy:**
- POST /rate — ocenianie eventu przez klienta

**Walidacje:**
- Rating 1-5, komentarz opcjonalny
- Geo-fence: ocena tylko w promieniu eventu
- Jeden rating na claim

**Przepływy:**
- Klient wchodzi w geo-fence → aplikacja pozwala ocenić → POST /rate → rating zapisany w Claim

**Testy:**
- Jednostkowe i integracyjne ratingu, edge-case'y

---

## 4. User (Użytkownik)

**Odpowiedzialność:**
- Rejestracja, logowanie (SMS/hasło), profil, role, rating
- Obsługa JWT

**DTO:**
- UserDto (id, name, phone, role, rating?, ratingCount?)

**Encje:**
- User (id, phone, roles, ratingAvg?, ratingCount?)

**Endpointy:**
- POST /users — rejestracja
- POST /auth/login — logowanie (SMS/hasło)
- GET /users/me — pobieranie profilu

**Walidacje:**
- Unikalność numeru telefonu
- Role: VENDOR, CUSTOMER, ADMIN
- Hasło (hashowane)

**Przepływy:**
- Rejestracja → walidacja → zapis w DB
- Logowanie → weryfikacja hasła → generowanie JWT
- Pobranie profilu na podstawie JWT

**Testy:**
- Jednostkowe i integracyjne rejestracji, logowania, profilu

---

## 5. Notification (Powiadomienia push)

**Odpowiedzialność:**
- Rejestracja tokenów FCM, subskrypcje na powiadomienia
- Wysyłka push do użytkowników w promieniu 2km

**DTO:**
- NotificationSubscription (token, userId, radius)

**Encje:**
- NotificationSubscription (token, userId, radius)

**Endpointy:**
- POST /notifications/subscribe — rejestracja tokenu FCM
- (internal) — wysyłka powiadomień do subskrybentów

**Walidacje:**
- Poprawność tokenu, radius

**Przepływy:**
- Klient subskrybuje → token zapisany w DB
- Nowy event → backend wysyła push do subskrybentów w promieniu

**Testy:**
- Jednostkowe i integracyjne subskrypcji, wysyłki

---

## 6. AI (OpenAI Vision, GPT)

**Odpowiedzialność:**
- Generowanie tytułu/opisu eventu na podstawie obrazu (stub/mock w MVP)

**DTO:**
- VisionLabelDto (label, confidence)

**Endpointy:**
- (internal) — wywołanie serwisu AI do generowania tytułu/opisu

**Przepływy:**
- Vendor tworzy event → backend wywołuje AI → zapisuje tytuł/opis

**Testy:**
- Testy integracyjne mocka AI

---

## 7. Config/Security

**Odpowiedzialność:**
- Zabezpieczenie endpointów (role, JWT, autoryzacja)
- Konfiguracja ról, CORS, OpenAPI

**Endpointy:**
- (internal) — konfiguracja security, role, JWT

**Przepływy:**
- Każdy request przechodzi przez filtr JWT, sprawdzane role

**Testy:**
- Testy autoryzacji, dostępów

---

## 8. Dokumentacja, CI/CD, Docker

**Odpowiedzialność:**
- OpenAPI docs, Docker Compose, CI/CD workflows

**Przepływy:**
- Generowanie dokumentacji, uruchamianie backendu i bazy w Dockerze, automatyczne testy i buildy

**Testy:**
- Testy integracyjne, smoke tests 