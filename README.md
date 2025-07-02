# 📰 News Aggregator Website

A modern, full-stack news aggregation platform built with **Spring Boot 3.2.0**, **Java 21**, **MySQL**, and **JavaScript**. This project demonstrates advanced web development skills with features like personalized news feeds, user authentication, real-time updates, and an admin dashboard.

## 🚀 Features

### Core Features
- **📰 News Aggregation**: Collects news from multiple RSS feeds and APIs
- **🔍 Advanced Search**: Search articles by keywords, categories, and sources
- **👤 User Authentication**: Secure JWT-based authentication system
- **💾 Bookmarking**: Save and manage favorite articles
- **💬 Comments**: Interactive commenting system on articles
- **⚙️ Personalized Preferences**: Customize news feed based on interests
- **📊 Admin Dashboard**: Analytics and content management

### Technical Features
- **🔄 Real-time Updates**: Automated RSS feed fetching every 5 minutes
- **📱 Responsive Design**: Mobile-first, modern UI with Bootstrap 5
- **🔒 Security**: JWT authentication, password encryption, CORS protection
- **🗄️ Database**: MySQL with JPA/Hibernate ORM
- **🎨 Modern UI**: Clean, intuitive interface with smooth animations

## 🛠️ Technology Stack

### Backend
- **Java 21** - Latest LTS version with modern features
- **Spring Boot 3.2.0** - Latest stable application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **MySQL 8.0+** - Database
- **JWT 0.12.3** - Latest token-based authentication
- **Rome 2.1.0** - RSS feed parsing

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Styling and animations
- **JavaScript (ES6+)** - Dynamic functionality
- **Bootstrap 5** - Responsive UI framework
- **Font Awesome** - Icons

## 📋 Prerequisites

Before running this application, make sure you have:

- **Java 21** or higher (Download from [Eclipse Temurin](https://adoptium.net/))
- **MySQL 8.0** or higher
- **Maven 3.6** or higher (optional - project includes wrapper)
- **Git** (for cloning)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd news-aggregator
```

### 2. Verify Java 21 Installation
```bash
java -version
```
Should show Java 21.x.x

### 3. Database Setup
1. **Install MySQL** if not already installed
2. **Create a database**:
   ```sql
   CREATE DATABASE news_aggregator;
   ```
3. **Update database credentials** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 4. Build and Run

#### Option A: Using Maven Wrapper (Recommended)
```bash
# Windows
run.bat

# Or manually
mvnw.cmd spring-boot:run
```

#### Option B: Using System Maven
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 5. Access the Application
- **Frontend**: http://localhost:8080
- **API Base URL**: http://localhost:8080/api

## 📖 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/check-username` - Check username availability
- `GET /api/auth/check-email` - Check email availability

### Article Endpoints
- `GET /api/articles` - Get all articles (paginated)
- `GET /api/articles/{id}` - Get article by ID
- `GET /api/articles/search` - Search articles
- `GET /api/articles/category/{category}` - Get articles by category
- `GET /api/articles/source/{sourceName}` - Get articles by source
- `GET /api/articles/top-headlines` - Get top headlines
- `GET /api/articles/most-viewed` - Get most viewed articles
- `GET /api/articles/latest` - Get latest articles
- `POST /api/articles/{id}/like` - Like an article
- `POST /api/articles/{id}/share` - Share an article

### Bookmark Endpoints
- `GET /api/bookmarks` - Get user bookmarks
- `POST /api/bookmarks` - Add bookmark
- `DELETE /api/bookmarks` - Remove bookmark
- `GET /api/bookmarks/search` - Search bookmarks

### Comment Endpoints
- `GET /api/comments/article/{articleId}` - Get comments for article
- `GET /api/comments/user/{userId}` - Get user comments
- `POST /api/comments` - Add comment
- `POST /api/comments/{id}/like` - Like comment
- `GET /api/comments/pending` - Get pending comments (admin)
- `POST /api/comments/{id}/approve` - Approve comment (admin)

### Preference Endpoints
- `GET /api/preferences/user/{userId}` - Get user preferences
- `POST /api/preferences` - Add preference
- `DELETE /api/preferences/{id}` - Delete preference

### Admin Endpoints
- `GET /api/admin/analytics` - Get analytics data

## 🎯 Key Features Explained

### 1. Personalized News Feeds
Users can customize their news experience by:
- Selecting preferred categories (Technology, Sports, Politics, etc.)
- Choosing favorite news sources
- Adding keywords for specific topics
- The system automatically filters and prioritizes content based on preferences

### 2. Advanced Search and Filtering
- **Full-text search** across article titles, descriptions, and content
- **Category-based filtering** (Technology, Business, Entertainment, etc.)
- **Source-based filtering** (BBC, CNN, TechCrunch, etc.)
- **Date range filtering** for historical articles
- **Sorting options** (Latest, Most Viewed, Most Liked)

### 3. User Account Management
- **Secure registration** with email validation
- **JWT-based authentication** for secure sessions
- **Profile management** with customizable preferences
- **Bookmark management** for saved articles
- **Comment history** and moderation

### 4. Social Features
- **Comment system** with moderation capabilities
- **Like/unlike articles** and comments
- **Share articles** on social media
- **User engagement tracking**

### 5. Real-time Updates
- **Automated RSS feed fetching** every 5 minutes
- **News API integration** for additional sources
- **Live content updates** without page refresh
- **Notification system** for new articles

### 6. Admin Dashboard
- **Analytics overview** (users, articles, comments)
- **Content management** tools
- **User management** capabilities
- **System monitoring** and health checks

## 🗄️ Database Schema

### Core Entities
- **User**: User accounts and profiles
- **Article**: News articles with metadata
- **Bookmark**: User-saved articles
- **Comment**: User comments on articles
- **UserPreference**: User customization settings

### Relationships
- User → Bookmark (One-to-Many)
- User → Comment (One-to-Many)
- User → UserPreference (One-to-Many)
- Article → Bookmark (One-to-Many)
- Article → Comment (One-to-Many)

## 🔧 Configuration

### Application Properties
Key configuration options in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/news_aggregator
spring.datasource.username=root
spring.datasource.password=password

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# RSS Feeds
rss.feeds=https://feeds.bbci.co.uk/news/rss.xml,https://rss.cnn.com/rss/edition.rss

# News API
news.api.key=your-news-api-key-here
news.api.base-url=https://newsapi.org/v2
```

### Environment Variables
For production deployment, consider using environment variables:
```bash
export SPRING_DATASOURCE_PASSWORD=your_production_password
export JWT_SECRET=your_production_jwt_secret
export NEWS_API_KEY=your_news_api_key
```

## 🚀 Deployment

### Local Development
```bash
# Using Maven wrapper
mvnw.cmd spring-boot:run

# Using system Maven
mvn spring-boot:run
```

### Production Build
```bash
# Build the project
mvn clean package

# Run the JAR
java -jar target/news-aggregator-1.0.0.jar
```

### Docker Deployment (Optional)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
COPY target/news-aggregator-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🧪 Testing

### Manual Testing
1. **Start the application** using `run.bat` or `mvn spring-boot:run`
2. **Register a new user**
3. **Login and explore features**
4. **Test all endpoints** using Postman or browser

### API Testing with Postman
Import the following collection for testing:

```json
{
  "info": {
    "name": "News Aggregator API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "url": "http://localhost:8080/api/auth/register",
            "body": {
              "mode": "raw",
              "raw": "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          }
        }
      ]
    }
  ]
}
```

## 📱 Screenshots

### Homepage
![Homepage](screenshots/homepage.png)
*Modern, responsive homepage with article cards*

### User Dashboard
![Dashboard](screenshots/dashboard.png)
*Personalized user dashboard with bookmarks and preferences*

### Admin Panel
![Admin](screenshots/admin.png)
*Admin dashboard with analytics and management tools*

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)

## 🙏 Acknowledgments

- **Spring Boot** team for the excellent framework
- **Eclipse Temurin** for Java 21 distribution
- **Bootstrap** for the responsive UI components
- **Font Awesome** for the beautiful icons
- **News API** for providing news content
- **Rome** library for RSS feed parsing

## 📞 Support

If you have any questions or need help:
- Create an issue in the repository
- Email: your.email@example.com
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)

---

**⭐ Star this repository if you found it helpful!** 