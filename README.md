<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk" alt="Java" />
  <img src="https://img.shields.io/badge/MySQL-JDBC-4479A1?style=for-the-badge&logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Swing-GUI-007396?style=for-the-badge" alt="Swing" />
  <img src="https://img.shields.io/badge/DAO-Pattern-00C853?style=for-the-badge" alt="DAO" />
</p>

<h1 align="center">📚 LibraryOS Ultimate</h1>

<p align="center">
  <strong>A full-featured Library Management System with Java Swing GUI</strong><br/>
  <em>CRUD operations, MySQL persistence, and clean DAO architecture</em>
</p>

---

## 📖 About

**LibraryOS Ultimate** is a desktop library management system built with **Java** and **Swing**. It provides a complete book management workflow with a MySQL database backend, implementing the **DAO (Data Access Object)** design pattern for clean separation of concerns.

---

## ✨ Features

- 📖 **Book Management** — Full CRUD (Create, Read, Update, Delete) operations
- 🔍 **Search & Filter** — Find books by title, author, or ISBN
- 💾 **MySQL Database** — Persistent storage with JDBC connection
- 🖥️ **Dashboard UI** — Java Swing graphical interface
- 🏗️ **DAO Pattern** — Clean architecture with separated data access layer
- 📦 **JAR Distribution** — Packaged as runnable JAR file

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 17+ |
| **GUI** | Java Swing |
| **Database** | MySQL (via XAMPP) |
| **Connectivity** | JDBC (mysql-connector-java) |
| **Architecture** | DAO Pattern (Model-DAO-UI) |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ (JDK)
- MySQL Server (XAMPP recommended)
- MySQL Connector/J JAR

### Database Setup
```sql
CREATE DATABASE library_db;
USE library_db;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20),
    year INT,
    available BOOLEAN DEFAULT TRUE
);
```

### Run the Application
```bash
# Compile
javac -cp ".:mysql-connector-java.jar" src/**/*.java

# Run
java -cp ".:src:mysql-connector-java.jar" Main
```

Or use the pre-built JAR:
```bash
java -jar LibraryOS_Ultimatee.jar
```

---

## 📁 Project Structure

```
LibraryOS_Ultimatee/
├── src/
│   ├── Main.java              # Application entry point
│   ├── model/
│   │   └── Book.java          # Book entity class
│   ├── dao/
│   │   └── BookDAO.java       # Database operations (CRUD)
│   └── ui/
│       └── LibraryDashboard.java  # Swing GUI
└── out/                       # Compiled classes
```

---

<p align="center">
  Built with ❤️ by <strong>Khalid Sayed</strong>
</p>
