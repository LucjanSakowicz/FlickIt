import json
import requests
import getpass
import os

try:
    from dotenv import load_dotenv
    DOTENV_AVAILABLE = True
except ImportError:
    DOTENV_AVAILABLE = False

BACKEND_TASKS_FILE = os.path.join(os.path.dirname(__file__), 'backend-tasks.json')
FRONTEND_TASKS_FILE = os.path.join(os.path.dirname(__file__), 'frontend-tasks.json')
ENV_FILE = os.path.join(os.path.dirname(__file__), '.env')

JIRA_API_URL = 'https://lucjansakowicz.atlassian.net/rest/api/3/issue/bulk'


def load_env():
    env = {}
    if DOTENV_AVAILABLE and os.path.exists(ENV_FILE):
        load_dotenv(ENV_FILE)
        env['email'] = os.getenv('JIRA_EMAIL')
        env['api_token'] = os.getenv('JIRA_API_TOKEN')
        env['project_key'] = os.getenv('JIRA_PROJECT_KEY')
    return env


def load_tasks():
    with open(BACKEND_TASKS_FILE, encoding='utf-8') as f:
        backend = json.load(f)
    with open(FRONTEND_TASKS_FILE, encoding='utf-8') as f:
        frontend = json.load(f)
    return backend, frontend


def get_credentials(env):
    # If .env is present, require all variables, else prompt for all
    if env.get('email') and env.get('api_token') and env.get('project_key'):
        return env['email'], env['api_token'], env['project_key']
    elif os.path.exists(ENV_FILE):
        missing = []
        if not env.get('email'):
            missing.append('JIRA_EMAIL')
        if not env.get('api_token'):
            missing.append('JIRA_API_TOKEN')
        if not env.get('project_key'):
            missing.append('JIRA_PROJECT_KEY')
        raise RuntimeError(f"Brakuje zmiennych w pliku .env: {', '.join(missing)}")
    else:
        print('Nie znaleziono pliku .env, podaj dane ręcznie.')
        email = input('Jira email: ')
        api_token = getpass.getpass('Jira API token: ')
        project_key = input('Jira project key (e.g. FLIC): ')
        return email, api_token, project_key


def select_tasks(tasks, label):
    print(f'\nAvailable {label} tasks:')
    for i, t in enumerate(tasks):
        print(f'{i+1}. {t["summary"]} [{", ".join(t["tags"])}]')
    selected = input(f'Enter comma-separated numbers to create (or "all"): ')
    if selected.strip().lower() == 'all':
        return tasks
    idxs = [int(x.strip())-1 for x in selected.split(',') if x.strip().isdigit()]
    return [tasks[i] for i in idxs if 0 <= i < len(tasks)]


# Helper to convert plain text to Atlassian Document Format (ADF)
def to_adf(description_text):
    return {
        "type": "doc",
        "version": 1,
        "content": [
            {
                "type": "paragraph",
                "content": [
                    {
                        "type": "text",
                        "text": description_text or ""
                    }
                ]
            }
        ]
    }


def build_issues(tasks, project_key):
    issues = []
    for t in tasks:
        # Combine main description and subtasks into a single string
        desc = t["description"]
        if t.get("subtasks"):
            desc += '\n\n' + '\n'.join(f'- {sub["summary"]}: {sub["description"]}' for sub in t["subtasks"])
        issue = {
            "fields": {
                "project": {"key": project_key},
                "summary": t["summary"],
                "description": to_adf(desc),
                "issuetype": {"name": "Task"},
                "labels": t["tags"]
            }
        }
        issues.append(issue)
    return issues


def create_issues(email, api_token, issues):
    headers = {
        "Content-Type": "application/json"
    }
    auth = (email, api_token)
    data = {"issueUpdates": issues}
    resp = requests.post(JIRA_API_URL, headers=headers, auth=auth, json=data)
    if resp.status_code == 201:
        print('Issues created successfully!')
        print(resp.json())
    else:
        print(f'Error: {resp.status_code} {resp.text}')


def main():
    if not DOTENV_AVAILABLE:
        print('python-dotenv is not installed. Install it with: pip install python-dotenv')
    env = load_env()
    backend, frontend = load_tasks()
    try:
        email, api_token, project_key = get_credentials(env)
    except RuntimeError as e:
        print(f'Błąd: {e}')
        return
    while True:
        print('\nWhich tasks do you want to create?')
        print('1. Backend')
        print('2. Frontend')
        print('3. Exit')
        choice = input('Select: ')
        if choice == '1':
            selected = select_tasks(backend, 'backend')
            issues = build_issues(selected, project_key)
            create_issues(email, api_token, issues)
        elif choice == '2':
            selected = select_tasks(frontend, 'frontend')
            issues = build_issues(selected, project_key)
            create_issues(email, api_token, issues)
        elif choice == '3':
            break
        else:
            print('Invalid choice.')

if __name__ == '__main__':
    main() 