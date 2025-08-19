FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install --no-cache-dir requests python-dotenv
CMD ["python", "jira_bulk_create.py"] 