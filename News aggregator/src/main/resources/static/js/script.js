// Utility for modals
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

// Utility for toast
let toastTimeout;
function showToast(title, message) {
    const toast = document.getElementById('toast');
    const toastTitle = document.getElementById('toastTitle');
    const toastBody = document.getElementById('toastBody');

    if (toast && toastTitle && toastBody) {
        toastTitle.innerText = title;
        toastBody.innerText = message;
        toast.classList.remove('hidden');
        toast.classList.add('toast-show');

        clearTimeout(toastTimeout);
        toastTimeout = setTimeout(() => {
            hideToast();
        }, 3000);
    }
}

function hideToast() {
    const toast = document.getElementById('toast');
    if (toast) {
        toast.classList.add('hidden');
        toast.classList.remove('toast-show');
    }
}


// News Aggregator Frontend Logic
const API_BASE = ''; // This should be an empty string or '/' if context path is root
let authToken = localStorage.getItem('token') || null;
let currentUser = localStorage.getItem('username') || null;
let currentUserId = localStorage.getItem('userId') || null;

// Store the currently active article ID in the modal
let currentModalArticleId = null;
let currentModalArticleIsRead = false;

// GLOBAL STATE for current fetch parameters (useful for re-fetching after actions)
let currentEndpoint = '/articles/latest';
let currentPage = 0;
let currentParams = {};
let currentReadStatusFilter = 'ALL';


// Utility: Set Auth UI based on login status
function setAuthUI() {
    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    const usernameSpan = document.getElementById('username');
    const readStatusFilter = document.getElementById('read-status-filter');

    if (authButtons && userMenu && usernameSpan) {
        if (authToken && currentUser) {
            authButtons.classList.add('hidden');
            userMenu.classList.remove('hidden');
            usernameSpan.innerText = currentUser;
            // Show read status filter for logged-in users
            if (readStatusFilter) readStatusFilter.classList.remove('hidden');
        } else {
            authButtons.classList.remove('hidden');
            userMenu.classList.add('hidden');
            // Hide read status filter for logged-out users
            if (readStatusFilter) readStatusFilter.classList.add('hidden');
        }
    }
}

/**
 * Fetches articles from the backend with various filters.
 * @param {string} endpoint The API endpoint (e.g., '/articles/latest').
 * @param {number} page The page number (0-indexed).
 * @param {object} params Additional query parameters (e.g., keyword, category).
 * @param {string} readStatusFilter 'ALL', 'READ', 'UNREAD' for filtering by read status.
 */
async function fetchArticles(endpoint = '/articles/latest', page = 0, params = {}, readStatusFilter = 'ALL') {
    // Update global state
    currentEndpoint = endpoint;
    currentPage = page;
    currentParams = params;
    currentReadStatusFilter = readStatusFilter;


    const loadingIndicator = document.getElementById('loadingSpinner');
    if (loadingIndicator) {
        loadingIndicator.classList.remove('hidden');
    }

    const articlesContainer = document.getElementById('articlesContainer');
    if (articlesContainer) {
        articlesContainer.innerHTML = '';
    }

    try {
        const url = new URL(`${window.location.origin}${API_BASE}${endpoint}`);
        url.searchParams.append('page', page);
        url.searchParams.append('size', 12);

        for (const key in params) {
            if (params.hasOwnProperty(key)) {
                url.searchParams.append(key, params[key]);
            }
        }

        // Add readStatusFilter to params if logged in and not 'ALL'
        if (authToken && readStatusFilter && readStatusFilter !== 'ALL') {
            url.searchParams.append('readStatus', readStatusFilter);
        } else {
            // Ensure readStatus parameter is removed if not needed (e.g., on logout)
            url.searchParams.delete('readStatus');
        }

        const headers = {};
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
        }

        const res = await fetch(url.toString(), { headers: headers });
        if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.message || `HTTP error! status: ${res.status}`);
        }
        const data = await res.json();
        renderArticles(data.content || []);
        renderPagination(data, endpoint, params, readStatusFilter); // Pass readStatusFilter to pagination
    } catch (error) {
        console.error("Error fetching articles:", error);
        if (articlesContainer) {
            articlesContainer.innerHTML = `<div class="col-span-full text-center text-red-600 p-4">
                <p>Failed to load news. Please try again later.</p>
                <p>Error: ${error.message}</p>
            </div>`;
        }
        showToast('Error', 'Failed to load news. See console for details.');
    } finally {
        if (loadingIndicator) {
            loadingIndicator.classList.add('hidden');
        }
    }
}

/**
 * Generates HTML for social sharing buttons.
 * @param {string} articleUrl The URL of the article to share.
 * @param {string} articleTitle The title of the article to share.
 * @returns {string} HTML string for social sharing buttons.
 */
function createSocialShareButtons(articleUrl, articleTitle) {
    const encodedUrl = encodeURIComponent(articleUrl);
    const encodedTitle = encodeURIComponent(articleTitle);

    const twitterUrl = `https://twitter.com/intent/tweet?url=${encodedUrl}&text=${encodedTitle}`;
    const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`;
    const linkedinUrl = `https://www.linkedin.com/shareArticle?mini=true&url=${encodedUrl}&title=${encodedTitle}`;
    const whatsappUrl = `https://api.whatsapp.com/send?text=${encodedTitle}%20${encodedUrl}`;

    return `
        <button class="share-btn bg-gray-800 hover:bg-black text-white px-3 py-2 rounded-full shadow-md transition duration-300" onclick="window.open('${twitterUrl}', '_blank', 'width=600,height=400')">
            <i class="fab fa-x-twitter"></i>
        </button>
        <button class="share-btn bg-blue-700 hover:bg-blue-800 text-white px-3 py-2 rounded-full shadow-md transition duration-300" onclick="window.open('${facebookUrl}', '_blank', 'width=600,height=400')">
            <i class="fab fa-facebook-f"></i>
        </button>
        <button class="share-btn bg-blue-800 hover:bg-blue-900 text-white px-3 py-2 rounded-full shadow-md transition duration-300" onclick="window.open('${linkedinUrl}', '_blank', 'width=600,height=600')">
            <i class="fab fa-linkedin-in"></i>
        </button>
        <button class="share-btn bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-full shadow-md transition duration-300" onclick="window.open('${whatsappUrl}', '_blank')">
            <i class="fab fa-whatsapp"></i>
        </button>
    `;
}

// Fetch and display article details in modal
async function showArticleDetail(articleId) {
    const modalArticleTitle = document.getElementById('modalArticleTitle');
    const modalArticleImage = document.getElementById('modalArticleImage');
    const modalArticleSummary = document.getElementById('modalArticleSummary');
    const modalReadFullArticleBtn = document.getElementById('modalReadFullArticleBtn');
    const toggleReadStatusBtn = document.getElementById('toggleReadStatusBtn');

    // Clear previous content and set loading placeholders
    modalArticleTitle.innerText = 'Loading...';
    // Using placehold.co for loading image
    modalArticleImage.src = 'https://placehold.co/600x400/E0E0E0/333333?text=Loading...';
    modalArticleSummary.innerText = 'Loading article summary...';
    modalReadFullArticleBtn.href = '#';
    modalReadFullArticleBtn.classList.add('pointer-events-none', 'opacity-50');

    // Hide read status toggle if not logged in
    if (!authToken) {
        if (toggleReadStatusBtn) toggleReadStatusBtn.classList.add('hidden');
    } else {
        if (toggleReadStatusBtn) toggleReadStatusBtn.classList.remove('hidden');
        toggleReadStatusBtn.innerText = 'Loading status...';
        toggleReadStatusBtn.className = 'px-4 py-2 rounded-lg font-semibold transition duration-300 bg-gray-300 text-gray-700';
    }

    showModal('articleDetailModal');

    try {
        const headers = {};
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
        }
        const res = await fetch(`${window.location.origin}${API_BASE}/articles/${articleId}`, { headers: headers });
        if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.message || `HTTP error! status: ${res.status}`);
        }
        const article = await res.json();

        // Populate modal with fetched data
        modalArticleTitle.innerText = article.title;
        // Check article.imageUrl for validity, otherwise use a placehold.co placeholder
        modalArticleImage.src = (article.imageUrl && (article.imageUrl.startsWith('http://') || article.imageUrl.startsWith('https://')))
                                 ? article.imageUrl
                                 : 'https://placehold.co/600x400/E0E0E0/333333?text=No+Image+Available'; // Corrected to placehold.co
        modalArticleSummary.innerText = article.summary || article.description || 'No summary available for this article.';
        modalReadFullArticleBtn.href = article.url;
        modalReadFullArticleBtn.classList.remove('pointer-events-none', 'opacity-50');

        currentModalArticleId = article.id;
        currentModalArticleIsRead = article.isRead || false;

        if (authToken && toggleReadStatusBtn) {
            updateToggleReadStatusButton(currentModalArticleIsRead);
            toggleReadStatusBtn.onclick = () => toggleArticleReadStatus(currentModalArticleId, !currentModalArticleIsRead);
        }

    } catch (error) {
        console.error("Error fetching article details for modal:", error);
        modalArticleTitle.innerText = 'Error Loading Article';
        // Using placehold.co for error image
        modalArticleImage.src = 'https://placehold.co/600x400/E0E0E0/333333?text=Error';
        modalArticleSummary.innerText = 'Failed to load article details. Please try again or visit the original source directly.';
        modalReadFullArticleBtn.href = '#';
        modalReadFullArticleBtn.classList.add('pointer-events-none', 'opacity-50');
        if (toggleReadStatusBtn) toggleReadStatusBtn.classList.add('hidden');
        showToast('Error', 'Failed to load article details.');
    }
}

// Helper function to update the toggle button's text and style
function updateToggleReadStatusButton(isRead) {
    const toggleReadStatusBtn = document.getElementById('toggleReadStatusBtn');
    if (!toggleReadStatusBtn) return;

    if (isRead) {
        toggleReadStatusBtn.innerText = 'Mark as Unread';
        toggleReadStatusBtn.className = 'px-4 py-2 rounded-lg font-semibold transition duration-300 bg-orange-500 hover:bg-orange-600 text-white';
    } else {
        toggleReadStatusBtn.innerText = 'Mark as Read';
        toggleReadStatusBtn.className = 'px-4 py-2 rounded-lg font-semibold transition duration-300 bg-green-500 hover:bg-green-600 text-white';
    }
}

// Function to toggle an article's read status
async function toggleArticleReadStatus(articleId, newStatus) {
    if (!authToken || !currentUserId) {
        showToast('Login Required', 'Please login to mark articles.');
        showModal('loginModal');
        return;
    }

    try {
        const res = await fetch(`${window.location.origin}${API_BASE}/articles/${articleId}/read-status?markAsRead=${newStatus}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!res.ok) {
            let errorMessage = `HTTP error! status: ${res.status}`;
            try {
                const errorData = await res.json();
                errorMessage = errorData.message || errorMessage;
            } catch (jsonError) {
                // If parsing JSON fails, just use the generic error message
            }
            throw new Error(errorMessage);
        }

        // Update the current modal's state and button
        currentModalArticleIsRead = newStatus;
        updateToggleReadStatusButton(newStatus);

        // Find the article card on the main page and update its class
        const articleCard = document.querySelector(`[data-article-id="${articleId}"]`);
        if (articleCard) {
            if (newStatus) {
                articleCard.classList.add('article-read');
                articleCard.classList.remove('article-unread');
            } else {
                articleCard.classList.remove('article-read');
                articleCard.classList.add('article-unread');
            }
        }

        showToast('Status Updated', `Article marked as ${newStatus ? 'read' : 'unread'}.`);
        // Re-fetch articles to refresh the list based on current filter
        fetchArticles(currentEndpoint, currentPage, currentParams, currentReadStatusFilter);

    } catch (error) {
        console.error("Error toggling read status:", error);
        showToast('Error', error.message || 'Failed to update read status.');
    }
}


// Render Articles in the UI
function renderArticles(articles) {
    const articlesContainer = document.getElementById('articlesContainer');
    if (!articlesContainer) return;

    articlesContainer.innerHTML = '';

    if (!articles.length) {
        articlesContainer.innerHTML = '<div class="col-span-full text-center text-gray-600 text-lg p-4">No articles found.</div>';
        return;
    }

    articles.forEach(article => {
        // Aggressive replacement for problematic via.placeholder.com URLs from backend response
        if (article.imageUrl && article.imageUrl.includes('via.placeholder.com')) {
            console.warn("Replacing problematic via.placeholder.com URL from backend response.");
            article.imageUrl = 'https://placehold.co/600x350/E0E0E0/333333?text=News+Image';
        }

        // Prioritize actual article properties, fall back to sensible defaults
        const articleTitle = article.title || article.newsTitle || 'Untitled Article'; // Check for common variations
        const articleDescription = article.description || article.summary || 'No description available.'; // Check for common variations
        const articleDate = article.publishedDate ? new Date(article.publishedDate).toLocaleDateString() : 'N/A';

        // Ensure that the image URL is valid and starts with http(s), otherwise use placehold.co
        const articleImage = (article.imageUrl && (article.imageUrl.startsWith('http://') || article.imageUrl.startsWith('https://')))
                             ? article.imageUrl
                             : `https://placehold.co/600x350/E0E0E0/333333?text=News+Image`;

        // Safely access source name, handle cases where 'source' might be null or undefined
        const sourceName = article.source && article.source.name ? article.source.name : 'Unknown Source';

        // Determine read status class
        const isReadClass = article.isRead ? 'article-read' : '';

        articlesContainer.innerHTML += `
            <div class="bg-white rounded-xl shadow-lg overflow-hidden news-card flex flex-col ${isReadClass}" data-article-id="${article.id}">
                <img src="${articleImage}" alt="${articleTitle}" class="w-full h-48 object-cover" onerror="this.onerror=null;this.src='https://placehold.co/600x350/E0E0E0/333333?text=Image+Not+Found';">
                <div class="p-5 flex-grow flex flex-col">
                    <h3 class="text-xl font-bold text-gray-900 mb-2 cursor-pointer" onclick="showArticleDetail(${article.id})">${articleTitle}</h3>
                    <p class="text-gray-700 text-sm mb-4 line-clamp-3">${articleDescription}</p>
                    <div class="flex items-center justify-between text-xs text-gray-500 mt-auto pt-2 border-t border-gray-100">
                        <span>Source: ${sourceName}</span>
                        <span>Date: ${articleDate}</span>
                        ${authToken ? `<span class="ml-2">${article.isRead ? '<i class="fas fa-eye text-gray-500"></i> Read' : '<i class="fas fa-eye-slash text-blue-500"></i> Unread'}</span>` : ''}
                    </div>
                    <div class="flex items-center justify-between mt-4">
                        <button onclick="showArticleDetail(${article.id})" class="inline-block text-blue-600 hover:text-blue-800 font-semibold transition duration-200">Read Summary &rarr;</button>
                        <button class="p-2 rounded-full text-gray-500 hover:text-blue-600 hover:bg-gray-100 transition-colors duration-200" onclick="bookmarkArticle(${article.id})">
                            <i class="fa fa-bookmark"></i>
                        </button>
                    </div>
                    <div class="flex justify-start items-center gap-2 mt-4 flex-wrap">
                        ${createSocialShareButtons(article.url || '#', articleTitle)}
                    </div>
                </div>
            </div>
        `;
    });
}

// Render Pagination Controls
function renderPagination(data, currentEndpoint, currentParams, readStatusFilter) {
    const paginationUl = document.getElementById('pagination');
    if (!paginationUl) return;

    paginationUl.innerHTML = '';

    if (!data.totalPages || data.totalPages <= 1) {
        return;
    }

    // Previous button
    if (data.number > 0) {
        paginationUl.innerHTML += `
            <li><button class="px-3 py-2 rounded-lg text-gray-600 hover:bg-blue-100" onclick="fetchArticles('${currentEndpoint}', ${data.number - 1}, ${JSON.stringify(currentParams)}, '${readStatusFilter}')">Previous</button></li>
        `;
    }

    // Page numbers
    const maxPagesToShow = 5;
    let startPage = Math.max(0, data.number - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(data.totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationUl.innerHTML += `
            <li><button class="px-3 py-2 rounded-lg ${i === data.number ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-blue-100'}" onclick="fetchArticles('${currentEndpoint}', ${i}, ${JSON.stringify(currentParams)}, '${readStatusFilter}')">${i + 1}</button></li>
        `;
    }

    // Next button
    if (data.number < data.totalPages - 1) {
        paginationUl.innerHTML += `
            <li><button class="px-3 py-2 rounded-lg text-gray-600 hover:bg-blue-100" onclick="fetchArticles('${currentEndpoint}', ${data.number + 1}, ${JSON.stringify(currentParams)}, '${readStatusFilter}')">Next</button></li>
        `;
    }
}


// Search News triggered by search bar or button
function searchNews() {
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('category-filter');
    const readStatusFilter = document.getElementById('read-status-filter');

    const keyword = searchInput ? searchInput.value : '';
    const category = categoryFilter ? categoryFilter.value : '';
    const readStatus = readStatusFilter ? readStatusFilter.value : 'ALL';

    let endpoint = '/articles/search';
    let params = {};
    if (keyword) {
        params.keyword = keyword;
    }
    if (category) {
        params.category = category;
    }
    fetchArticles(endpoint, 0, params, readStatus);
}

// Load Top Headlines
function loadTopHeadlines() {
    const feedTitle = document.getElementById('feed-title');
    if (feedTitle) feedTitle.innerText = 'Top Headlines';
    const readStatusFilter = document.getElementById('read-status-filter');
    const readStatus = readStatusFilter ? readStatusFilter.value : 'ALL';
    fetchArticles('/articles/top-headlines', 0, {}, readStatus);
    setActiveNavButton('topHeadlinesBtn');
}

// Load Most Viewed
function loadMostViewed() {
    const feedTitle = document.getElementById('feed-title');
    if (feedTitle) feedTitle.innerText = 'Most Viewed';
    const readStatusFilter = document.getElementById('read-status-filter');
    const readStatus = readStatusFilter ? readStatusFilter.value : 'ALL';
    fetchArticles('/articles/most-viewed', 0, {}, readStatus);
    setActiveNavButton('mostViewedBtn');
}

// Load Latest News
function loadLatestNews() {
    const feedTitle = document.getElementById('feed-title');
    if (feedTitle) feedTitle.innerText = 'Latest Headlines';
    const readStatusFilter = document.getElementById('read-status-filter');
    const readStatus = readStatusFilter ? readStatusFilter.value : 'ALL';
    fetchArticles('/articles/latest', 0, {}, readStatus);
    setActiveNavButton('latestNewsBtn');
}

// Set active navigation button style
function setActiveNavButton(activeId) {
    const buttons = ['topHeadlinesBtn', 'mostViewedBtn', 'latestNewsBtn'];
    buttons.forEach(id => {
        const btn = document.getElementById(id);
        if (btn) {
            if (id === activeId) {
                btn.classList.remove('bg-gray-200', 'text-gray-800', 'hover:bg-gray-300');
                btn.classList.add('bg-blue-600', 'text-white', 'hover:bg-blue-700', 'font-semibold');
            } else {
                btn.classList.remove('bg-blue-600', 'text-white', 'hover:bg-blue-700', 'font-semibold');
                btn.classList.add('bg-gray-200', 'text-gray-800', 'hover:bg-gray-300');
            }
        }
    });
}


// Bookmark Article
async function bookmarkArticle(articleId) {
    if (!authToken || !currentUserId) {
        showToast('Login Required', 'Please login to bookmark articles.');
        showModal('loginModal');
        return;
    }

    try {
        const res = await fetch(`${window.location.origin}${API_BASE}/bookmarks?articleId=${articleId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (res.ok) {
            showToast('Bookmarked', 'Article bookmarked successfully!');
        } else if (res.status === 409) {
            showToast('Already Bookmarked', 'This article is already in your bookmarks.');
        } else {
            const err = await res.json();
            throw new Error(err.message || 'Could not bookmark article.');
        }
    } catch (error) {
        console.error("Error bookmarking article:", error);
        showToast('Error', error.message || 'Could not bookmark article.');
    }
}

// Show Personalized Feed
async function showPersonalizedFeed() {
    if (!authToken || !currentUserId) {
        showToast('Login Required', 'Please login to view your personalized feed.');
        showModal('loginModal');
        return;
    }
    const feedTitle = document.getElementById('feed-title');
    if (feedTitle) feedTitle.innerText = 'My Personalized Feed';
    fetchArticles(`/articles/personalized`, 0, {}, 'ALL');
    setActiveNavButton('');
    // Close the dropdown after the action
    const userDropdown = document.getElementById('userDropdown');
    if (userDropdown) userDropdown.classList.add('hidden');
}

// Show My Bookmarks
async function showMyBookmarks() {
    if (!authToken || !currentUserId) {
        showToast('Login Required', 'Please login to view your bookmarks.');
        showModal('loginModal');
        return;
    }
    const feedTitle = document.getElementById('feed-title');
    if (feedTitle) feedTitle.innerText = 'My Bookmarks';

    const articlesContainer = document.getElementById('articlesContainer');
    if (articlesContainer) articlesContainer.innerHTML = ''; // Clear container
    const loadingIndicator = document.getElementById('loadingSpinner');
    if (loadingIndicator) loadingIndicator.classList.remove('hidden');

    try {
        // Step 1: Fetch bookmark IDs
        const bookmarkRes = await fetch(`${window.location.origin}${API_BASE}/bookmarks/my-bookmarks`, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });

        if (!bookmarkRes.ok) {
            const errorData = await bookmarkRes.json();
            throw new Error(errorData.message || `HTTP error! status: ${bookmarkRes.status}`);
        }
        const bookmarkData = await bookmarkRes.json();
        const bookmarkedArticleIds = bookmarkData.content.map(b => b.id);

        if (bookmarkedArticleIds.length === 0) {
            if (articlesContainer) {
                articlesContainer.innerHTML = '<div class="col-span-full text-center text-gray-600 text-lg p-4">No bookmarks found.</div>';
            }
            if (loadingIndicator) loadingIndicator.classList.add('hidden');
            document.getElementById('pagination').innerHTML = ''; // Clear pagination
            return;
        }

        // Step 2: Fetch full details for each bookmarked article concurrently
        const articlePromises = bookmarkedArticleIds.map(id =>
            fetch(`${window.location.origin}${API_BASE}/articles/${id}`, {
                headers: { 'Authorization': `Bearer ${authToken}` }
            }).then(res => {
                if (!res.ok) {
                    // Log error but don't stop other fetches
                    console.error(`Failed to fetch article ${id}: ${res.status}`);
                    return null; // Return null for failed fetches
                }
                return res.json();
            }).catch(err => {
                console.error(`Error fetching article ${id}:`, err);
                return null;
            })
        );

        const fullArticles = (await Promise.all(articlePromises)).filter(article => article !== null);

        // Ensure 'isRead' status is present for bookmarked articles if backend doesn't provide it directly
        // For consistency, let's ensure it's marked as read by default for display if not explicitly set.
        fullArticles.forEach(article => {
            if (article && typeof article.isRead === 'undefined') {
                article.isRead = true; // Assume bookmarked articles are "read" by default, adjust if needed
            }
        });


        // Step 3: Render the full articles
        renderArticles(fullArticles);
        renderPagination(bookmarkData, '/bookmarks/my-bookmarks', {}, 'ALL'); // Pagination for bookmark meta-data

    } catch (error) {
        console.error("Error fetching my bookmarks:", error);
        if (articlesContainer) {
            articlesContainer.innerHTML = `<div class="col-span-full text-center text-red-600 p-4">
                <p>Failed to load bookmarks. Please try again later.</p>
                <p>Error: ${error.message}</p>
            </div>`;
        }
        showToast('Error', 'Failed to load bookmarks. See console for details.');
    } finally {
        if (loadingIndicator) {
            loadingIndicator.classList.add('hidden');
        }
        // Close the dropdown after the action
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown) userDropdown.classList.add('hidden');
    }
    setActiveNavButton(''); // Deselect other buttons
}

// Event listeners attached once DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const userMenuBtn = document.getElementById('userMenuBtn');
    const userDropdown = document.getElementById('userDropdown');
    const switchToRegisterBtn = document.getElementById('switchToRegister');
    const switchToLoginBtn = document.getElementById('switchToLogin');
    const logoutBtn = document.getElementById('logoutBtn');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const closeArticleDetailModalBtn = document.getElementById('closeArticleDetailModalBtn');
    const readStatusFilter = document.getElementById('read-status-filter');


    if (closeArticleDetailModalBtn) {
        closeArticleDetailModalBtn.addEventListener('click', () => hideModal('articleDetailModal'));
    }

    if (loginBtn) loginBtn.addEventListener('click', () => showModal('loginModal'));
    if (registerBtn) registerBtn.addEventListener('click', () => showModal('registerModal'));

    if (userMenuBtn) {
        userMenuBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (userDropdown) userDropdown.classList.toggle('hidden');
        });
    }

    if (switchToRegisterBtn) {
        switchToRegisterBtn.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal('loginModal');
            showModal('registerModal');
        });
    }

    if (switchToLoginBtn) {
        switchToLoginBtn.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal('registerModal');
            showModal('loginModal');
        });
    }

    // Close dropdown if clicked outside
    window.addEventListener('click', function(event) {
        if (userDropdown && !userDropdown.contains(event.target) && !event.target.matches('#userMenuBtn') && !event.target.closest('#userMenuBtn')) {
            if (!userDropdown.classList.contains('hidden')) {
                userDropdown.classList.add('hidden');
            }
        }
    });

    if (logoutBtn) logoutBtn.addEventListener('click', () => {
        authToken = null;
        currentUser = null;
        currentUserId = null;
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        setAuthUI();
        showToast('Logged Out', 'You have been logged out.');
        loadLatestNews(); // Re-load news without auth token
        // Close the dropdown after logout
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown) userDropdown.classList.add('hidden');
    });

    if (loginForm) {
        loginForm.onsubmit = async function(e) {
            e.preventDefault();
            const usernameInput = document.getElementById('loginUsername');
            const passwordInput = document.getElementById('loginPassword');
            const username = usernameInput ? usernameInput.value : '';
            const password = passwordInput ? passwordInput.value : '';

            try {
                const res = await fetch(`${window.location.origin}${API_BASE}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });
                const data = await res.json();
                if (res.ok) {
                    authToken = data.accessToken;
                    currentUser = data.username;
                    currentUserId = data.id;
                    localStorage.setItem('token', authToken);
                    localStorage.setItem('username', currentUser);
                    localStorage.setItem('userId', currentUserId);
                    setAuthUI();
                    showToast('Login Success', 'Welcome back!');
                    hideModal('loginModal');
                    loadLatestNews(); // Re-load news with auth token
                } else {
                    showToast('Login Failed', data.message || 'Invalid credentials');
                }
            } catch (error) {
                console.error("Login error:", error);
                showToast('Error', 'An error occurred during login.');
            }
        };
    }

    if (registerForm) {
        registerForm.onsubmit = async function(e) {
            e.preventDefault();
            const username = document.getElementById('registerUsername').value;
            const email = document.getElementById('registerEmail').value;
            const password = document.getElementById('registerPassword').value;
            const firstName = document.getElementById('registerFirstName').value;
            const lastName = document.getElementById('registerLastName').value;
            try {
                const res = await fetch(`${window.location.origin}${API_BASE}/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, email, password, firstName, lastName })
                });
                const data = await res.json();
                if (res.ok) {
                    showToast('Registration Success', data.message || 'You can now login.');
                    hideModal('registerModal');
                } else {
                    showToast('Registration Failed', data.message || 'Could not register');
                }
            } catch (error) {
                console.error("Registration error:", error);
                showToast('Error', 'An error occurred during registration.');
            }
        };
    }

    // Main Content Navigation Buttons
    const topHeadlinesBtn = document.getElementById('topHeadlinesBtn');
    const mostViewedBtn = document.getElementById('mostViewedBtn');
    const latestNewsBtn = document.getElementById('latestNewsBtn');

    if (topHeadlinesBtn) topHeadlinesBtn.addEventListener('click', loadTopHeadlines);
    if (mostViewedBtn) mostViewedBtn.addEventListener('click', loadMostViewed);
    if (latestNewsBtn) latestNewsBtn.addEventListener('click', loadLatestNews);

    // Search and Filter Events
    const searchButton = document.getElementById('search-button');
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('category-filter');

    if (searchButton) searchButton.addEventListener('click', searchNews);
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchNews();
            }
        });
    }

    // Event listener for the read status filter
    if (readStatusFilter) {
        readStatusFilter.addEventListener('change', () => {
            const params = { ...currentParams };
            fetchArticles(currentEndpoint, currentPage, params, readStatusFilter.value);
        });
    }

    const personalizedFeedBtn = document.getElementById('personalizedFeedBtn');
    const myBookmarksBtn = document.getElementById('myBookmarksBtn');

    if (personalizedFeedBtn) personalizedFeedBtn.addEventListener('click', () => {
        showPersonalizedFeed();
        // Close the dropdown
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown) userDropdown.classList.add('hidden');
    });
    if (myBookmarksBtn) myBookmarksBtn.addEventListener('click', () => {
        showMyBookmarks();
        // The dropdown closing is already handled inside showMyBookmarks's finally block
    });

    // Initial load on page load
    setAuthUI();
    loadLatestNews();

    // Check for 'feed-title' element and create if not found.
    if (!document.getElementById('feed-title')) {
        const h2 = document.querySelector('main h2');
        if (h2) {
            h2.id = 'feed-title';
        } else {
            console.warn("Could not find or create a 'feed-title' element. News feed titles might not update.");
        }
    }
});