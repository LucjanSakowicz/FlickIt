# FlickIt

## Jira: Masowe tworzenie zadań z plików JSON

W folderze `project-tasks/` znajduje się skrypt `jira_bulk_create.py`, który umożliwia szybkie tworzenie zadań w projekcie Jira na podstawie plików `backend-tasks.json` i `frontend-tasks.json`.

### Wymagania
- Python 3.x
- Biblioteka `requests` oraz `python-dotenv` (`pip install requests python-dotenv`)
- Konto w Jira Cloud oraz wygenerowany token API ([instrukcja](https://id.atlassian.com/manage-profile/security/api-tokens))

### Konfiguracja zmiennych środowiskowych
1. Skopiuj plik `.env.example` do `.env` w folderze `project-tasks/` i uzupełnij swoimi danymi:
   ```
   cp .env.example .env
   # Edytuj plik .env i wpisz swój e-mail, token i klucz projektu
   ```
2. Alternatywnie, dane możesz podać ręcznie przy uruchomieniu skryptu (jeśli nie ma .env).

### Instrukcja użycia lokalnie
1. Przejdź do folderu projektu:
   ```
   cd project-tasks
   ```
2. Zainstaluj wymagane biblioteki:
   ```
   pip install requests python-dotenv
   ```
3. Uruchom skrypt:
   ```
   python jira_bulk_create.py
   ```
4. Wybierz, które zadania chcesz utworzyć (backend/frontend, pojedynczo lub wszystkie).

### Instrukcja użycia w Dockerze
1. Przejdź do folderu `project-tasks/`:
   ```
   cd project-tasks
   ```
2. Zbuduj obraz:
   ```
   docker build -t jira-bulk .
   ```
3. Uruchom kontener (upewnij się, że masz plik `.env` z danymi):
   ```
   docker run --rm -it -v $(pwd):/app jira-bulk
   ```

### Dodatkowe informacje
- Zadania są importowane z plików JSON (`backend-tasks.json`, `frontend-tasks.json`).
- Każde zadanie zawiera szczegółowy opis i podzadania w opisie.
- Skrypt można uruchamiać wielokrotnie i tworzyć zadania na żądanie.

W razie potrzeby rozbudowy skryptu (np. obsługa epików, komponentów, automatyczne przypisywanie) — zgłoś zapotrzebowanie w backlogu! 