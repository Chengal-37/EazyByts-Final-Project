package com.newsaggregator.service; // <<< IMPORTANT: ENSURE THIS MATCHES YOUR PROJECT'S ACTUAL BASE PACKAGE FOR SERVICES

import com.newsaggregator.model.Article; // Adjust if your model package is different, e.g., com.newsaggregator.model
import com.newsaggregator.model.Source;// Adjust if your model package is different
import com.newsaggregator.service.dto.newsapi.NewsApiResponse; // Adjust if your DTO package is different

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException; // Added for more specific date parsing error handling
import java.util.Arrays; // Added for Collections.singletonList
import java.util.List;

// Jsoup imports for HTML parsing
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

// Imports for SSL bypass
import javax.net.ssl.SSLException;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import io.netty.handler.ssl.SslContext; // Explicitly import Netty's SslContext


@Service
public class NewsFeedIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(NewsFeedIngestionService.class);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SourceService sourceService;

    private WebClient webClient; // For NewsAPI
    private WebClient insecureRssWebClient; // For RSS feeds with SSL bypass

    @Value("${rss.feeds}")
    private String rssFeedsConfig;

    @Value("${news.api.key}")
    private String newsApiKey;

    @Value("${news.api.base-url}")
    private String newsApiBaseUrl;

    private final WebClient.Builder webClientBuilder;

    // IMPORTANT FOR TESTING:
    // This list controls what topics are fetched from NewsAPI.
    // For specific BBC News testing, you might want to uncomment one of the options below
    // or manually specify "sources=bbc-news" in the URL in ingestFromNewsApi.
    // Ensure you revert this for production if you want a wider range of news.
    private final List<String> NEWS_API_DEFAULT_TOPICS = Arrays.asList("technology"); // Original, limited for debugging
    // private final List<String> NEWS_API_DEFAULT_TOPICS = Arrays.asList("general", "business", "technology", "entertainment", "health", "science", "sports"); // More comprehensive
    // private final List<String> NEWS_API_DEFAULT_TOPICS = Collections.singletonList("general"); // For quick test with a broader category
    // private final List<String> NEWS_API_DEFAULT_TOPICS = Collections.singletonList("bbc news"); // NewsAPI 'q' parameter might not be exact for sources

    public NewsFeedIngestionService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.baseUrl(newsApiBaseUrl).build();

        // Initialize a separate WebClient that ignores SSL certificate validation
        // THIS IS FOR TESTING ONLY AND SHOULD NOT BE USED IN PRODUCTION
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // TRUST ALL CERTS (INSECURE)
                    .build(); // Correctly returns Netty's SslContext

            HttpClient httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)); // Pass the pre-built Netty sslContext

            this.insecureRssWebClient = webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } catch (SSLException e) {
            logger.error("Failed to initialize insecureRssWebClient due to an SSL error: {}", e.getMessage());
            logger.error("Stack trace:", e);
            // The insecureRssWebClient will remain null, subsequent calls to ingestFromRssFeeds will be blocked.
        } catch (Exception e) {
            logger.error("Failed to initialize insecureRssWebClient due to an unexpected error: {}", e.getMessage());
            logger.error("Stack trace:", e);
        }
    }

    @Scheduled(fixedRateString = "${news.ingestion.rate.ms:1800000}") // Default 30 minutes (1800000 ms)
    @Transactional
    public void ingestNewsFeeds() {
        logger.info("Starting news ingestion process...");

        // Ensure these are called based on your configuration/needs
        ingestFromRssFeeds();
        ingestFromNewsApi();

        logger.info("News ingestion process completed.");
    }

    private void ingestFromRssFeeds() {
        if (rssFeedsConfig == null || rssFeedsConfig.isEmpty()) {
            logger.warn("No RSS feeds configured. Skipping RSS ingestion.");
            return;
        }
        // Check if insecureRssWebClient was successfully initialized
        if (this.insecureRssWebClient == null) {
            logger.error("insecureRssWebClient was not initialized successfully during startup. Cannot ingest from RSS feeds.");
            return;
        }


        List<String> rssUrls = Arrays.asList(rssFeedsConfig.split(","));
        for (String urlString : rssUrls) {
            final String currentRssUrl = urlString.trim();
            if (currentRssUrl.isEmpty()) continue;

            try {
                logger.info("Fetching RSS feed content from: {}", currentRssUrl);
                // Use the insecureRssWebClient for RSS feeds
                String rssContent = insecureRssWebClient.get() // Using the insecure client
                        .uri(currentRssUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // Blocking call in scheduled task

                if (rssContent == null || rssContent.isEmpty()) {
                    logger.warn("Received empty content for RSS feed: {}", currentRssUrl);
                    continue;
                }

                // Parse the fetched content using ROME
                SyndFeedInput input = new SyndFeedInput();
                input.setAllowDoctypes(false); // Recommended security practice for ROME

                // Use StringReader to provide the fetched content to ROME
                SyndFeed feed = input.build(new StringReader(rssContent));

                logger.info("Ingesting from RSS feed: {}", feed.getTitle());

                Source source = sourceService.getSourceByName(feed.getTitle())
                        .orElseGet(() -> {
                            Source newSource = new Source();
                            newSource.setName(feed.getTitle());
                            // Use feed.getLink() for base URL, it's typically more reliable than rssFeedUrl itself
                            newSource.setBaseUrl(feed.getLink());
                            newSource.setRssFeedUrl(currentRssUrl);
                            return sourceService.saveSource(newSource);
                        });

                for (SyndEntry entry : feed.getEntries()) {
                    try {
                        Article article = new Article();
                        article.setTitle(entry.getTitle());
                        article.setUrl(entry.getLink());
                        
                        if (entry.getDescription() != null) {
                            article.setDescription(entry.getDescription().getValue());
                        } else {
                            logger.debug("RSS entry '{}' has no description.", entry.getTitle());
                        }

                        if (entry.getPublishedDate() != null) {
                            article.setPublishedDate(LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                        } else {
                            article.setPublishedDate(LocalDateTime.now());
                            logger.debug("RSS entry '{}' has no published date, using current time.", entry.getTitle());
                        }

                        String extractedImageUrl = null;

                        // Attempt 1: From Enclosures (Standard RSS)
                        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
                            extractedImageUrl = entry.getEnclosures().stream()
                                .filter(enc -> enc.getType() != null && enc.getType().startsWith("image/"))
                                .map(enc -> enc.getUrl())
                                .findFirst()
                                .orElse(null);
                            if (extractedImageUrl != null) {
                                logger.debug("Image found in Enclosures for '{}': {}", entry.getTitle(), extractedImageUrl);
                            } else {
                                logger.debug("No image found in Enclosures (filtered by type) for '{}'. Full enclosures: {}", entry.getTitle(), entry.getEnclosures());
                            }
                        } else {
                            logger.debug("No Enclosures found for '{}'.", entry.getTitle());
                        }

                        // Attempt 2: Use Jsoup to find image in description HTML
                        if (extractedImageUrl == null && entry.getDescription() != null && entry.getDescription().getValue() != null) {
                            try {
                                Document doc = Jsoup.parse(entry.getDescription().getValue());
                                Elements imgElements = doc.select("img[src]"); // Select <img> tags with a 'src' attribute
                                if (!imgElements.isEmpty()) {
                                    extractedImageUrl = imgElements.first().attr("src");
                                    logger.debug("Image found in description HTML via Jsoup for '{}': {}", entry.getTitle(), extractedImageUrl);
                                } else {
                                    logger.debug("No image found in description HTML via Jsoup for '{}'.", entry.getTitle());
                                }
                            } catch (Exception e) {
                                logger.warn("Error parsing description HTML for image using Jsoup for '{}': {}", entry.getTitle(), e.getMessage());
                            }
                        }

                        // Attempt 3: Use Jsoup to find image in content HTML (if available)
                        if (extractedImageUrl == null && entry.getContents() != null && !entry.getContents().isEmpty()) {
                            String contentValue = entry.getContents().get(0).getValue();
                            try {
                                Document doc = Jsoup.parse(contentValue);
                                Elements imgElements = doc.select("img[src]");
                                if (!imgElements.isEmpty()) {
                                    extractedImageUrl = imgElements.first().attr("src");
                                    logger.debug("Image found in content HTML via Jsoup for '{}': {}", entry.getTitle(), extractedImageUrl);
                                    // Additional: If an entry URL is available, try to resolve relative image URLs
                                    if (extractedImageUrl != null && !extractedImageUrl.startsWith("http") && entry.getLink() != null) {
                                        try {
                                            // Using URI and then toURL for robust relative URL resolution
                                            URI baseUri = new URI(entry.getLink());
                                            URI resolvedUri = baseUri.resolve(extractedImageUrl);
                                            extractedImageUrl = resolvedUri.toURL().toExternalForm();
                                            logger.debug("Resolved relative image URL for '{}': {}", entry.getTitle(), extractedImageUrl);
                                        } catch (URISyntaxException | MalformedURLException ex) {
                                            logger.warn("Could not resolve relative image URL '{}' for '{}': {}", extractedImageUrl, entry.getTitle(), ex.getMessage());
                                        }
                                    }
                                } else {
                                    logger.debug("No image found in content HTML via Jsoup for '{}'. Content snippet: {}", entry.getTitle(), contentValue.substring(0, Math.min(contentValue.length(), 200)));
                                }
                            } catch (Exception e) {
                                logger.warn("Error parsing content HTML for image using Jsoup for '{}': {}", entry.getTitle(), e.getMessage());
                            }
                        } else {
                            logger.debug("No content or empty content for '{}' to parse for images.", entry.getTitle());
                        }

                        // RSS articles usually have images or descriptions that can be parsed for images.
                        // If no image is found after all attempts, use a fallback.
                        if (extractedImageUrl == null || extractedImageUrl.isEmpty() || extractedImageUrl.contains("via.placeholder.com")) {
                             article.setImageUrl("https://placehold.co/600x350/E0E0E0/333333?text=News+Image");
                             logger.debug("Using placeholder image for RSS article '{}'. Original URL was: {}", article.getTitle(), extractedImageUrl);
                        } else {
                            article.setImageUrl(extractedImageUrl);
                        }

                        if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
                            article.setCategory(entry.getCategories().get(0).getName());
                        } else {
                            article.setCategory("General");
                            logger.debug("No category found for '{}', setting to 'General'.", entry.getTitle());
                        }
                        if (entry.getAuthor() != null) {
                            article.setAuthor(entry.getAuthor());
                        } else {
                            logger.debug("No author found for '{}'.", entry.getTitle());
                        }

                        article.setSource(source);
                        articleService.saveArticle(article);
                        logger.debug("Saved RSS article: {} with imageUrl: {}", article.getTitle(), article.getImageUrl());

                    } catch (Exception e) {
                        logger.error("Failed to process RSS entry: {}. Error: {}", entry.getTitle(), e.getMessage());
                        logger.error("Stack trace for RSS entry processing error:", e);
                    }
                }
            } catch (org.springframework.web.reactive.function.client.WebClientRequestException e) {
                 logger.error("Failed to fetch RSS feed content from {} using WebClient. Error: {}", currentRssUrl, e.getMessage());
                 logger.error("Stack trace for WebClientRequestException:", e); // Add stack trace for WebClient issues
            } catch (FeedException e) {
                logger.error("Failed to parse RSS feed from {}. Error: {}", currentRssUrl, e.getMessage());
                logger.error("Stack trace for FeedException:", e); // Add stack trace for parsing issues
            } catch (Exception e) { // Catch any other unexpected exceptions during RSS fetching/parsing
                logger.error("An unexpected error occurred during RSS ingestion from {}. Error: {}", currentRssUrl, e.getMessage());
                logger.error("Stack trace for unexpected error:", e); // Log stack trace for unexpected errors
            }
        }
    }

    private void ingestFromNewsApi() {
        if (newsApiKey == null || newsApiKey.isEmpty() || "your-news-api-key-here".equals(newsApiKey)) {
            logger.warn("News API key is not configured. Skipping News API ingestion.");
            return;
        }

        for (String topic : NEWS_API_DEFAULT_TOPICS) {
            // IMPORTANT FOR TESTING:
            // To specifically test BBC News, you can uncomment one of the lines below
            // and comment out the current 'url' line.
            // Option 1: Query by specific source (if your NewsAPI plan allows this with '/top-headlines')
            // String url = String.format("/top-headlines?sources=bbc-news&apiKey=%s", newsApiKey);
            // Option 2: Query 'BBC News' specifically using the 'q' parameter in '/everything'
            // This might yield results from various sources mentioning "BBC News".
            // String url = String.format("/everything?q=BBC News&apiKey=%s", newsApiKey);
            // Option 3: Continue with existing topic (e.g., "technology" or "general") and observe BBC results within it
            String url = String.format("/everything?q=%s&apiKey=%s", topic, newsApiKey);


            logger.info("Fetching articles from News API for topic/source: {}", topic);

            try {
                NewsApiResponse response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(NewsApiResponse.class)
                        .block();

                if (response != null && response.getArticles() != null) {
                    logger.info("Received {} articles from NewsAPI for topic/source '{}'.", response.getArticles().size(), topic);
                    for (com.newsaggregator.service.dto.newsapi.Article newsApiArticle : response.getArticles()) {
                        try {
                            if (newsApiArticle.getTitle() == null || newsApiArticle.getUrl() == null) {
                                logger.warn("Skipping News API article due to missing title or URL: {}", newsApiArticle);
                                continue;
                            }

                            String sourceName = (newsApiArticle.getSource() != null && newsApiArticle.getSource().getName() != null)
                                    ? newsApiArticle.getSource().getName() : "Unknown News API Source";
                            
                            Source source = sourceService.getSourceByName(sourceName)
                                    .orElseGet(() -> {
                                        Source newSource = new Source();
                                        newSource.setName(sourceName);
                                        // Use article URL as base URL for source, or a more generic one if available
                                        newSource.setBaseUrl(extractDomain(newsApiArticle.getUrl()));
                                        newSource.setApiKey(newsApiKey); // Store API key with source if needed
                                        return sourceService.saveSource(newSource);
                                    });

                            Article article = new Article(); // Your internal Article model
                            article.setTitle(newsApiArticle.getTitle());
                            article.setDescription(newsApiArticle.getDescription());
                            article.setUrl(newsApiArticle.getUrl());

                            // --- CRITICAL LOGGING AND IMAGE URL FALLBACK LOGIC ---
                            String imageUrlFromNewsApi = newsApiArticle.getUrlToImage();

                            // Log the image URL received from NewsAPI, especially for BBC News
                            if (sourceName.equals("BBC News")) { // Assuming "BBC News" is the exact name from NewsAPI
                                logger.info("BBC News Article: '{}', Raw NewsAPI imageUrl: '{}'",
                                            newsApiArticle.getTitle(), imageUrlFromNewsApi);
                            } else {
                                // For other sources, debug level is fine
                                logger.debug("Article: '{}', Source: '{}', Raw NewsAPI imageUrl: '{}'",
                                            newsApiArticle.getTitle(), sourceName, imageUrlFromNewsApi);
                            }

                            // Implement the fallback logic here
                            if (imageUrlFromNewsApi == null || imageUrlFromNewsApi.isEmpty() || imageUrlFromNewsApi.contains("via.placeholder.com")) {
                                article.setImageUrl("https://placehold.co/600x350/E0E0E0/333333?text=News+Image");
                                logger.debug("Using placeholder image for NewsAPI article '{}' (Source: {}). Original URL was: {}",
                                             newsApiArticle.getTitle(), sourceName, imageUrlFromNewsApi);
                            } else {
                                article.setImageUrl(imageUrlFromNewsApi);
                            }
                            // --- END CRITICAL LOGGING AND IMAGE URL FALLBACK LOGIC ---


                            if (newsApiArticle.getPublishedAt() != null) {
                                try {
                                    Instant instant = Instant.parse(newsApiArticle.getPublishedAt());
                                    article.setPublishedDate(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                                } catch (DateTimeParseException dtpe) {
                                    logger.warn("Could not parse published date '{}' for article '{}'. Setting to now. Error: {}",
                                                newsApiArticle.getPublishedAt(), newsApiArticle.getTitle(), dtpe.getMessage());
                                    article.setPublishedDate(LocalDateTime.now());
                                }
                            } else {
                                article.setPublishedDate(LocalDateTime.now());
                            }

                            article.setAuthor(newsApiArticle.getAuthor());
                            // NewsAPI's 'everything' endpoint doesn't return categories directly.
                            // You might infer it from the 'topic' or leave it as "General".
                            article.setCategory(topic); // Assign the queried topic as the category
                            article.setSource(source);

                            articleService.saveArticle(article);
                            logger.debug("Saved NewsAPI article: {} with final imageUrl: {}", article.getTitle(), article.getImageUrl());

                        } catch (Exception e) {
                            logger.error("Failed to process News API article ({}). Error: {}",
                                         newsApiArticle != null ? newsApiArticle.getTitle() : "Unknown",
                                         e.getMessage(), e); // Log stack trace
                        }
                    }
                } else {
                    logger.warn("NewsAPI response for topic/source '{}' was null or contained no articles.", topic);
                }
            } catch (Exception e) {
                logger.error("Failed to fetch news from News API for topic/source {}. Error: {}", topic, e.getMessage(), e); // Log stack trace
            }
        }
    }

    // Helper method to extract domain from a URL for source base URL
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                // Remove www. prefix if present
                if (host.startsWith("www.")) {
                    return host.substring(4);
                }
                return host;
            }
        } catch (URISyntaxException e) {
            logger.warn("Could not parse URL for domain extraction: {}", url, e);
        }
        return null;
    }
}