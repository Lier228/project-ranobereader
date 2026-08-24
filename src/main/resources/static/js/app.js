const applicationState = {
    roles: [],
    users: [],
    currentUser: null,
    catalog: [],
    currentPage: 0,
    totalPages: 1,
    pageSize: 8,
    genres: [],
    selectedGenreId: null,
    currentNovel: null,
    chapters: [],
    currentChapterIndex: 0,
    userBookmarks: [],
    bookmarkFilter: ""
};

$(document).ready(function () {
    restoreSession();
});

// TOKEN & AUTH UTILITIES
function getToken() {
    return localStorage.getItem("access_token");
}

function saveToken(token) {
    localStorage.setItem("access_token", token);
}

function removeToken() {
    localStorage.removeItem("access_token");
}

function getAuthorizationHeaders() {
    const token = getToken();
    if (!token) return {};
    return {
        Authorization: `Bearer ${token}`
    };
}

function isLoggedIn() {
    const token = getToken();
    const payload = getTokenPayload();
    return !!(token && payload);
}

function getTokenPayload() {
    const token = getToken();
    if (!token) return null;
    try {
        const parts = token.split(".");
        if (parts.length !== 3) return null;
        const normalized = parts[1].replace(/-/g, "+").replace(/_/g, "/");
        const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
        const decoded = decodeURIComponent(
            atob(padded)
                .split("")
                .map(character => "%" + ("00" + character.charCodeAt(0).toString(16)).slice(-2))
                .join("")
        );
        const payload = JSON.parse(decoded);
        if (payload.exp && payload.exp * 1000 <= Date.now()) {
            return null;
        }
        return payload;
    } catch (error) {
        return null;
    }
}

function hasRole(roleName) {
    const payload = getTokenPayload();
    return payload?.roles?.includes(roleName) ?? false;
}

function isTranslatorOrAdmin() {
    return hasRole("ROLE_TRANSLATOR") || hasRole("ROLE_SCANLATOR") || hasRole("ROLE_ADMIN");
}

// SESSION & NAVIGATION
function restoreSession() {
    loadGenres();
    loadCatalog(0);

    if (isLoggedIn()) {
        showLoggedInHeader();
        loadCurrentUser();
    } else {
        showGuestHeader();
    }
}

function showGuestHeader() {
    $("#header-guest").removeClass("hidden");
    $("#header-user").addClass("hidden");
    $("#nav-bookmarks-btn").addClass("hidden");
    $("#nav-admin-btn").addClass("hidden");
    $("#profile-card").addClass("hidden");
    $("#add-novel-btn").addClass("hidden");
    $("#add-chapter-btn").addClass("hidden");
}

function showLoggedInHeader() {
    $("#header-guest").addClass("hidden");
    $("#header-user").removeClass("hidden");
    $("#nav-bookmarks-btn").removeClass("hidden");

    if (hasRole("ROLE_ADMIN")) {
        $("#nav-admin-btn").removeClass("hidden");
        $("#admin-delete-novel-btn").removeClass("hidden");
    } else {
        $("#nav-admin-btn").addClass("hidden");
        $("#admin-delete-novel-btn").addClass("hidden");
    }

    if (isTranslatorOrAdmin()) {
        $("#add-novel-btn").removeClass("hidden");
        $("#add-chapter-btn").removeClass("hidden");
    } else {
        $("#add-novel-btn").addClass("hidden");
        $("#add-chapter-btn").addClass("hidden");
    }
}

function showAuthModal(type) {
    $("#authentication-section").removeClass("hidden");
    if (type === "register") {
        showRegisterForm();
    } else {
        showLoginForm();
    }
}

function closeAuthModal() {
    $("#authentication-section").addClass("hidden");
}

function showCatalogView() {
    setActiveNav("nav-catalog-btn");
    $("#catalog-view-section").removeClass("hidden");
    $("#reader-view-section").addClass("hidden");
    $("#bookmarks-view-section").addClass("hidden");
    $("#admin-section").addClass("hidden");
    loadCatalog(applicationState.currentPage);
}

function showBookmarksView() {
    if (!isLoggedIn()) {
        showMessage("Авторизуйтесь для доступа к закладкам", "warning");
        showAuthModal("login");
        return;
    }
    setActiveNav("nav-bookmarks-btn");
    $("#catalog-view-section").addClass("hidden");
    $("#reader-view-section").addClass("hidden");
    $("#bookmarks-view-section").removeClass("hidden");
    $("#admin-section").addClass("hidden");
    loadUserBookmarks();
}

function showAdminView() {
    if (!hasRole("ROLE_ADMIN")) {
        showMessage("Доступ запрещен", "danger");
        return;
    }
    setActiveNav("nav-admin-btn");
    $("#catalog-view-section").addClass("hidden");
    $("#reader-view-section").addClass("hidden");
    $("#bookmarks-view-section").addClass("hidden");
    $("#admin-section").removeClass("hidden");
    loadAdminData();
}

function setActiveNav(btnId) {
    $(".nav-button").removeClass("active");
    $("#" + btnId).addClass("active");
}

function showLoginForm() {
    $("#login-form").removeClass("hidden");
    $("#register-form").addClass("hidden");
    $("#login-tab").addClass("active");
    $("#register-tab").removeClass("active");
}

function showRegisterForm() {
    $("#login-form").addClass("hidden");
    $("#register-form").removeClass("hidden");
    $("#login-tab").removeClass("active");
    $("#register-tab").addClass("active");
}

// AUTHENTICATION AJAX
function registerUser() {
    const request = {
        fullName: $("#register-full-name").val().trim(),
        email: $("#register-email").val().trim(),
        password: $("#register-password").val(),
        repeatPassword: $("#register-repeat-password").val()
    };

    if (!request.fullName || !request.email || !request.password || !request.repeatPassword) {
        showMessage("Заполните все поля регистрации", "warning");
        return;
    }

    if (request.password.length < 6) {
        showMessage("Пароль должен содержать минимум 6 символов", "warning");
        return;
    }

    if (request.password !== request.repeatPassword) {
        showMessage("Пароли не совпадают", "warning");
        return;
    }

    $.ajax({
        url: "/api/auth/register",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),
        success: function () {
            $("#login-email").val(request.email);
            $("#login-password").val("");
            clearRegisterForm();
            showLoginForm();
            showMessage("Регистрация прошла успешно. Теперь выполните вход.", "success");
        },
        error: handleRequestError
    });
}

function login() {
    const request = {
        email: $("#login-email").val().trim(),
        password: $("#login-password").val()
    };

    if (!request.email || !request.password) {
        showMessage("Введите email и пароль", "warning");
        return;
    }

    $.ajax({
        url: "/api/auth/login",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),
        success: function (response) {
            saveToken(response.accessToken);
            closeAuthModal();
            showLoggedInHeader();
            loadCurrentUser();
            showMessage("Вход выполнен успешно", "success");
        },
        error: function (xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                showMessage("Неверный email или пароль", "danger");
            } else {
                handleRequestError(xhr);
            }
        }
    });
}

function logout() {
    removeToken();
    applicationState.roles = [];
    applicationState.users = [];
    applicationState.currentUser = null;
    applicationState.userBookmarks = [];
    
    showGuestHeader();
    showCatalogView();
    showMessage("Вы вышли из системы", "success");
}

// USER PROFILE
function loadCurrentUser() {
    if (!isLoggedIn()) return;
    $.ajax({
        url: "/api/users/me",
        type: "GET",
        headers: getAuthorizationHeaders(),
        success: function (user) {
            applicationState.currentUser = user;
            renderCurrentUser(user);
        },
        error: handleRequestError
    });
}

function renderCurrentUser(user) {
    $("#profile-card").removeClass("hidden");
    $("#profile-full-name").text(user.fullName);
    $("#profile-email").text(user.email);
    $("#header-user-name").text(user.fullName);
    $("#header-user-email").text(user.email);

    const firstLetter = user.fullName?.trim().charAt(0).toUpperCase() || "U";
    $("#profile-avatar").text(firstLetter);

    const rolesHtml = Array.from(user.roles)
        .map(role => `<span class="role-badge">${escapeHtml(role)}</span>`)
        .join("");
    $("#profile-roles").html(rolesHtml);
}

// TRANSLATOR MODALS & CREATION
function renderGenreCheckboxes(containerId, selectedGenreIds) {
    selectedGenreIds = selectedGenreIds || [];
    const html = applicationState.genres.map(g => {
        const checked = selectedGenreIds.includes(g.id) ? "checked" : "";
        return `
            <label style="display:inline-flex; align-items:center; gap:4px; font-size:13px; cursor:pointer; background:#f0f3f8; padding:4px 8px; border-radius:6px;">
                <input type="checkbox" class="genre-checkbox" value="${g.id}" ${checked}> ${escapeHtml(g.name)}
            </label>
        `;
    }).join("");
    $("#" + containerId).html(html);
}

function openAddNovelModal() {
    if (!isLoggedIn()) {
        showAuthModal("login");
        return;
    }
    renderGenreCheckboxes("new-novel-genres", []);
    $("#add-novel-modal").removeClass("hidden");
}

function closeAddNovelModal() {
    $("#add-novel-modal").addClass("hidden");
}

function submitAddNovel() {
    const selectedGenres = [];
    $("#new-novel-genres .genre-checkbox:checked").each(function () {
        selectedGenres.push(parseInt($(this).val()));
    });

    const request = {
        titleRu: $("#new-novel-title-ru").val().trim(),
        titleEn: $("#new-novel-title-en").val().trim(),
        slug: $("#new-novel-slug").val().trim().toLowerCase(),
        author: $("#new-novel-author").val().trim() || "Неизвестен",
        coverImage: $("#new-novel-cover").val().trim(),
        description: $("#new-novel-description").val().trim(),
        status: "ONGOING",
        genreIds: selectedGenres
    };

    if (!request.titleRu || !request.slug) {
        showMessage("Заполните название и URL slug", "warning");
        return;
    }

    $.ajax({
        url: "/api/novels",
        type: "POST",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify(request),
        success: function () {
            closeAddNovelModal();
            showMessage("Ранобэ успешно добавлено!", "success");
            loadCatalog(0);
        },
        error: handleRequestError
    });
}

function openEditNovelModal() {
    const novel = applicationState.currentNovel;
    if (!novel) return;

    $("#edit-novel-title-ru").val(novel.titleRu || "");
    $("#edit-novel-title-en").val(novel.titleEn || "");
    $("#edit-novel-slug").val(novel.slug || "");
    $("#edit-novel-author").val(novel.author || "");
    $("#edit-novel-cover").val(novel.coverImage || "");
    $("#edit-novel-status").val(novel.status || "ONGOING");
    $("#edit-novel-description").val(novel.description || "");

    const selectedIds = novel.genres ? Array.from(novel.genres).map(g => g.id) : [];
    renderGenreCheckboxes("edit-novel-genres", selectedIds);

    $("#edit-novel-modal").removeClass("hidden");
}

function closeEditNovelModal() {
    $("#edit-novel-modal").addClass("hidden");
}

function submitEditNovel() {
    const novel = applicationState.currentNovel;
    if (!novel) return;

    const selectedGenres = [];
    $("#edit-novel-genres .genre-checkbox:checked").each(function () {
        selectedGenres.push(parseInt($(this).val()));
    });

    const request = {
        titleRu: $("#edit-novel-title-ru").val().trim(),
        titleEn: $("#edit-novel-title-en").val().trim(),
        slug: $("#edit-novel-slug").val().trim().toLowerCase(),
        author: $("#edit-novel-author").val().trim() || "Неизвестен",
        coverImage: $("#edit-novel-cover").val().trim(),
        description: $("#edit-novel-description").val().trim(),
        status: $("#edit-novel-status").val(),
        genreIds: selectedGenres
    };

    if (!request.titleRu) {
        showMessage("Заполните название", "warning");
        return;
    }

    $.ajax({
        url: "/api/novels/" + novel.id,
        type: "PUT",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify(request),
        success: function (updated) {
            closeEditNovelModal();
            showMessage("Ранобэ успешно обновлено!", "success");
            applicationState.currentNovel = updated;
            renderNovelDetails(updated);
        },
        error: handleRequestError
    });
}

function openAddChapterModal() {
    if (!applicationState.currentNovel) return;
    if (!isLoggedIn()) {
        showAuthModal("login");
        return;
    }
    $("#add-chapter-modal").removeClass("hidden");
}

function closeAddChapterModal() {
    $("#add-chapter-modal").addClass("hidden");
}

function submitAddChapter() {
    const novelId = applicationState.currentNovel?.id;
    if (!novelId) return;

    const request = {
        novelId: novelId,
        tomeNumber: parseInt($("#new-chapter-tome").val()) || 1,
        chapterNumber: parseFloat($("#new-chapter-num").val()) || 1,
        title: $("#new-chapter-title").val().trim(),
        content: $("#new-chapter-content").val().trim()
    };

    if (!request.content) {
        showMessage("Введите текст главы", "warning");
        return;
    }

    $.ajax({
        url: "/api/novels/chapters",
        type: "POST",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify(request),
        success: function () {
            closeAddChapterModal();
            showMessage("Глава опубликована!", "success");
            loadChapters(novelId);
        },
        error: handleRequestError
    });
}

function openEditChapterModal() {
    const idx = applicationState.currentChapterIndex;
    const chapter = applicationState.chapters[idx];
    if (!chapter) return;

    $("#edit-chapter-tome").val(chapter.tomeNumber);
    $("#edit-chapter-num").val(chapter.chapterNumber);
    $("#edit-chapter-title").val(chapter.title || "");
    $("#edit-chapter-content").val(chapter.content || "");

    $("#edit-chapter-modal").removeClass("hidden");
}

function closeEditChapterModal() {
    $("#edit-chapter-modal").addClass("hidden");
}

function submitEditChapter() {
    const idx = applicationState.currentChapterIndex;
    const chapter = applicationState.chapters[idx];
    if (!chapter) return;

    const request = {
        novelId: chapter.novelId,
        tomeNumber: parseInt($("#edit-chapter-tome").val()) || 1,
        chapterNumber: parseFloat($("#edit-chapter-num").val()) || 1,
        title: $("#edit-chapter-title").val().trim(),
        content: $("#edit-chapter-content").val().trim()
    };

    if (!request.content) {
        showMessage("Введите текст главы", "warning");
        return;
    }

    $.ajax({
        url: "/api/novels/chapters/" + chapter.id,
        type: "PUT",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify(request),
        success: function () {
            closeEditChapterModal();
            showMessage("Глава успешно обновлена!", "success");
            loadChapters(chapter.novelId);
        },
        error: handleRequestError
    });
}

// NOVEL CATALOG
function loadGenres() {
    $.ajax({
        url: "/api/novels/genres",
        type: "GET",
        success: function (genres) {
            applicationState.genres = genres;
            renderGenres();
        }
    });
}

function renderGenres() {
    let html = `<button type="button" class="genre-pill ${applicationState.selectedGenreId === null ? 'active' : ''}" onclick="selectGenre(null)">Все жанры</button>`;
    html += applicationState.genres.map(g => `
        <button type="button" class="genre-pill ${applicationState.selectedGenreId === g.id ? 'active' : ''}" onclick="selectGenre(${g.id})">
            ${escapeHtml(g.name)}
        </button>
    `).join("");
    $("#genre-filter-container").html(html);
}

function selectGenre(genreId) {
    applicationState.selectedGenreId = genreId;
    renderGenres();
    loadCatalog(0);
}

function handleSearchKeyup(event) {
    if (event.key === "Enter") {
        loadCatalog(0);
    }
}

function loadCatalog(page) {
    page = page || 0;
    applicationState.currentPage = page;

    const query = $("#search-input").val() ? $("#search-input").val().trim() : "";
    let url = "/api/novels?page=" + page + "&size=" + applicationState.pageSize;
    if (query) url += "&query=" + encodeURIComponent(query);
    if (applicationState.selectedGenreId) url += "&genreId=" + applicationState.selectedGenreId;

    $.ajax({
        url: url,
        type: "GET",
        success: function (pageData) {
            applicationState.catalog = pageData.content;
            applicationState.currentPage = pageData.currentPage;
            applicationState.totalPages = pageData.totalPages;
            renderCatalog();
            renderPagination(pageData);
        },
        error: handleRequestError
    });
}

function renderCatalog() {
    if (!applicationState.catalog || applicationState.catalog.length === 0) {
        $("#novel-grid").html("<p>Ранобэ не найдено</p>");
        return;
    }

    const html = applicationState.catalog.map(n => {
        const cover = n.coverImage || "https://via.placeholder.com/220x300?text=No+Cover";
        const tags = n.genres ? Array.from(n.genres).map(g => `<span class="manga-tag">${escapeHtml(g.name)}</span>`).join("") : "";
        const ratingText = n.averageRating > 0 ? `${n.averageRating} ★` : "Нет оценок";
        return `
            <div class="manga-card" onclick="openNovelReader(${n.id})">
                <img src="${escapeHtml(cover)}" alt="${escapeHtml(n.titleRu)}" class="manga-cover" onerror="this.src='https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80'">
                <div class="manga-info">
                    <h3 class="manga-title">${escapeHtml(n.titleRu)}</h3>
                    <div class="manga-meta">
                        <span class="rating-badge">${ratingText}</span> • ${escapeHtml(n.author || 'Неизвестен')}
                    </div>
                    <div class="manga-tags">${tags}</div>
                </div>
            </div>
        `;
    }).join("");

    $("#novel-grid").html(html);
}

function renderPagination(pageData) {
    if (pageData.totalPages <= 1) {
        $("#catalog-pagination").empty();
        return;
    }

    let buttons = "";

    buttons += `<button type="button" class="page-number-btn" ${!pageData.hasPrevious ? 'disabled' : ''} onclick="loadCatalog(${pageData.currentPage - 1})">« Пред.</button>`;

    for (let i = 0; i < pageData.totalPages; i++) {
        const active = i === pageData.currentPage ? "active" : "";
        buttons += `<button type="button" class="page-number-btn ${active}" onclick="loadCatalog(${i})">${i + 1}</button>`;
    }

    buttons += `<button type="button" class="page-number-btn" ${!pageData.hasNext ? 'disabled' : ''} onclick="loadCatalog(${pageData.currentPage + 1})">След. »</button>`;

    $("#catalog-pagination").html(buttons);
}

// NOVEL DETAILS, RATING & READER
function openNovelReader(novelId) {
    $("#catalog-view-section").addClass("hidden");
    $("#bookmarks-view-section").addClass("hidden");
    $("#admin-section").addClass("hidden");
    $("#reader-view-section").removeClass("hidden");

    if (hasRole("ROLE_ADMIN")) {
        $("#admin-delete-novel-btn").removeClass("hidden");
    } else {
        $("#admin-delete-novel-btn").addClass("hidden");
    }

    if (isTranslatorOrAdmin()) {
        $("#edit-novel-btn").removeClass("hidden");
    } else {
        $("#edit-novel-btn").addClass("hidden");
    }

    loadNovelDetails(novelId);
    loadChapters(novelId);
    loadNovelRating(novelId);

    if (isLoggedIn()) {
        checkUserBookmarkForNovel(novelId);
    } else {
        $("#novel-bookmark-select").val("");
    }
}

function confirmDeleteCurrentNovel() {
    const novel = applicationState.currentNovel;
    if (!novel) return;

    if (!confirm(`Вы действительно хотите удалить ранобэ "${novel.titleRu}" и все его главы?`)) {
        return;
    }

    $.ajax({
        url: "/api/novels/" + novel.id,
        type: "DELETE",
        headers: getAuthorizationHeaders(),
        success: function () {
            showMessage("Ранобэ успешно удалено", "success");
            showCatalogView();
        },
        error: handleRequestError
    });
}

function loadNovelDetails(novelId) {
    $.ajax({
        url: "/api/novels/" + novelId,
        type: "GET",
        success: function (novel) {
            applicationState.currentNovel = novel;
            renderNovelDetails(novel);
        },
        error: handleRequestError
    });
}

function renderNovelDetails(novel) {
    const cover = novel.coverImage || "https://via.placeholder.com/180x250?text=No+Cover";
    const tags = novel.genres ? Array.from(novel.genres).map(g => `<span class="manga-tag">${escapeHtml(g.name)}</span>`).join("") : "";
    const html = `
        <img src="${escapeHtml(cover)}" alt="${escapeHtml(novel.titleRu)}" class="manga-details-cover" onerror="this.src='https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80'">
        <div class="manga-details-body" style="flex-grow: 1;">
            <h2>${escapeHtml(novel.titleRu)}</h2>
            ${novel.titleEn ? `<p class="user-email">${escapeHtml(novel.titleEn)}</p>` : ''}
            <div class="manga-meta" style="margin-top: 8px;">Автор: <strong>${escapeHtml(novel.author || 'Неизвестен')}</strong> | Статус: <strong>${escapeHtml(novel.status)}</strong></div>
            <div class="manga-tags" style="margin-top: 8px;">${tags}</div>
            
            <div class="star-rating-container">
                <div id="star-widget" class="star-rating">
                    <span class="star-btn" onclick="rateNovel(1)">★</span>
                    <span class="star-btn" onclick="rateNovel(2)">★</span>
                    <span class="star-btn" onclick="rateNovel(3)">★</span>
                    <span class="star-btn" onclick="rateNovel(4)">★</span>
                    <span class="star-btn" onclick="rateNovel(5)">★</span>
                </div>
                <div id="rating-summary" class="rating-summary-text">Загрузка оценки...</div>
            </div>

            <p class="manga-details-description">${escapeHtml(novel.description || 'Описание отсутствует.')}</p>
        </div>
    `;
    $("#novel-details-card").html(html);
}

function loadNovelRating(novelId) {
    const headers = isLoggedIn() ? getAuthorizationHeaders() : {};
    $.ajax({
        url: "/api/novels/" + novelId + "/rating",
        type: "GET",
        headers: headers,
        success: function (res) {
            renderStarRatingWidget(res);
        }
    });
}

function renderStarRatingWidget(res) {
    const avgText = res.totalRatings > 0 ? `${res.averageRating} / 5 (${res.totalRatings} оценок)` : "Нет оценок (будьте первым!)";
    $("#rating-summary").text(avgText);

    const userVal = res.userRating || 0;
    $("#star-widget .star-btn").each(function (idx) {
        if (idx < userVal) {
            $(this).addClass("star-active");
        } else {
            $(this).removeClass("star-active");
        }
    });
}

function rateNovel(val) {
    if (!isLoggedIn()) {
        showMessage("Авторизуйтесь, чтобы выставлять оценки новеллам", "warning");
        showAuthModal("login");
        return;
    }

    const novelId = applicationState.currentNovel?.id;
    if (!novelId) return;

    $.ajax({
        url: "/api/novels/" + novelId + "/rating",
        type: "POST",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify({ rating: val }),
        success: function (res) {
            renderStarRatingWidget(res);
            showMessage(`Вы поставили оценку ${val} ★`, "success");
        },
        error: handleRequestError
    });
}

function loadChapters(novelId) {
    $.ajax({
        url: "/api/novels/" + novelId + "/chapters",
        type: "GET",
        success: function (chapters) {
            applicationState.chapters = chapters;
            renderChapterSelect(chapters);
            if (chapters.length > 0) {
                applicationState.currentChapterIndex = 0;
                renderChapterContent(chapters[0]);
            } else {
                $("#chapter-reader-title").text("Главы отсутствуют");
                $("#chapter-text-content").text("В этой новелле пока нет опубликованных глав.");
                $("#prev-chapter-btn").prop("disabled", true);
                $("#next-chapter-btn").prop("disabled", true);
                $("#delete-chapter-btn").addClass("hidden");
            }
        },
        error: handleRequestError
    });
}

function renderChapterSelect(chapters) {
    const options = chapters.map((ch, idx) => `
        <option value="${idx}">Том ${ch.tomeNumber} Глава ${ch.chapterNumber} ${ch.title ? ': ' + escapeHtml(ch.title) : ''}</option>
    `).join("");
    $("#chapter-select").html(options);
}

function onChapterSelectChange() {
    const idx = parseInt($("#chapter-select").val());
    if (!isNaN(idx) && applicationState.chapters[idx]) {
        applicationState.currentChapterIndex = idx;
        renderChapterContent(applicationState.chapters[idx]);
    }
}

function renderChapterContent(chapter) {
    $("#chapter-reader-title").text(`Том ${chapter.tomeNumber} Глава ${chapter.chapterNumber}${chapter.title ? ': ' + chapter.title : ''}`);
    $("#chapter-text-content").text(chapter.content);

    if (isTranslatorOrAdmin()) {
        $("#delete-chapter-btn").removeClass("hidden");
        $("#edit-chapter-btn").removeClass("hidden");
    } else {
        $("#delete-chapter-btn").addClass("hidden");
        $("#edit-chapter-btn").addClass("hidden");
    }

    const idx = applicationState.currentChapterIndex;
    const chapters = applicationState.chapters;

    $("#chapter-select").val(idx);
    $("#prev-chapter-btn").prop("disabled", idx <= 0);
    $("#next-chapter-btn").prop("disabled", idx >= chapters.length - 1);
    
    window.scrollTo({ top: $("#reader-view-section").offset().top - 80, behavior: 'smooth' });
}

function confirmDeleteCurrentChapter() {
    const idx = applicationState.currentChapterIndex;
    const chapter = applicationState.chapters[idx];
    if (!chapter) return;

    if (!confirm(`Удалить главу "Том ${chapter.tomeNumber} Глава ${chapter.chapterNumber}"?`)) {
        return;
    }

    $.ajax({
        url: "/api/novels/chapters/" + chapter.id,
        type: "DELETE",
        headers: getAuthorizationHeaders(),
        success: function () {
            showMessage("Глава успешно удалена", "success");
            loadChapters(chapter.novelId);
        },
        error: handleRequestError
    });
}

function nextChapter() {
    if (applicationState.currentChapterIndex < applicationState.chapters.length - 1) {
        applicationState.currentChapterIndex++;
        renderChapterContent(applicationState.chapters[applicationState.currentChapterIndex]);
    }
}

function prevChapter() {
    if (applicationState.currentChapterIndex > 0) {
        applicationState.currentChapterIndex--;
        renderChapterContent(applicationState.chapters[applicationState.currentChapterIndex]);
    }
}

// BOOKMARKS
function checkUserBookmarkForNovel(novelId) {
    $.ajax({
        url: "/api/bookmarks",
        type: "GET",
        headers: getAuthorizationHeaders(),
        success: function (bookmarks) {
            const found = bookmarks.find(b => b.novelId === novelId);
            if (found) {
                $("#novel-bookmark-select").val(found.status);
            } else {
                $("#novel-bookmark-select").val("");
            }
        }
    });
}

function updateNovelBookmark() {
    if (!isLoggedIn()) {
        $("#novel-bookmark-select").val("");
        showMessage("Авторизуйтесь, чтобы добавлять новеллы в закладки", "warning");
        showAuthModal("login");
        return;
    }

    const status = $("#novel-bookmark-select").val();
    const novelId = applicationState.currentNovel?.id;
    if (!novelId) return;

    if (!status) {
        $.ajax({
            url: "/api/bookmarks/" + novelId,
            type: "DELETE",
            headers: getAuthorizationHeaders(),
            success: function () {
                showMessage("Удалено из закладок", "success");
            },
            error: handleRequestError
        });
    } else {
        $.ajax({
            url: "/api/bookmarks",
            type: "POST",
            contentType: "application/json",
            headers: getAuthorizationHeaders(),
            data: JSON.stringify({ novelId: novelId, status: status }),
            success: function () {
                showMessage("Закладка обновлена", "success");
            },
            error: handleRequestError
        });
    }
}

function loadUserBookmarks() {
    let url = "/api/bookmarks";
    if (applicationState.bookmarkFilter) {
        url += "?status=" + applicationState.bookmarkFilter;
    }

    $.ajax({
        url: url,
        type: "GET",
        headers: getAuthorizationHeaders(),
        success: function (bookmarks) {
            applicationState.userBookmarks = bookmarks;
            renderBookmarks();
        },
        error: handleRequestError
    });
}

function filterBookmarks(status) {
    applicationState.bookmarkFilter = status;
    $(".bookmark-tab").removeClass("active");
    $(event.target).addClass("active");
    loadUserBookmarks();
}

function renderBookmarks() {
    if (!applicationState.userBookmarks || applicationState.userBookmarks.length === 0) {
        $("#bookmarks-grid").html("<p>У вас пока нет закладок в этой категории.</p>");
        return;
    }

    const html = applicationState.userBookmarks.map(b => {
        const n = b.novel;
        const cover = n.coverImage || "https://via.placeholder.com/220x300?text=No+Cover";
        return `
            <div class="manga-card" onclick="openNovelReader(${n.id})">
                <img src="${escapeHtml(cover)}" alt="${escapeHtml(n.titleRu)}" class="manga-cover" onerror="this.src='https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80'">
                <div class="manga-info">
                    <h3 class="manga-title">${escapeHtml(n.titleRu)}</h3>
                    <div class="manga-meta">Статус закладки: <span class="role-badge">${escapeHtml(b.status)}</span></div>
                </div>
            </div>
        `;
    }).join("");

    $("#bookmarks-grid").html(html);
}

// ADMIN DASHBOARD
function loadAdminData() {
    loadRoles();
}

function loadRoles() {
    $.ajax({
        url: "/api/admin/roles",
        type: "GET",
        headers: getAuthorizationHeaders(),
        success: function (roles) {
            applicationState.roles = roles;
            renderRoles();
            loadUsers();
        },
        error: handleRequestError
    });
}

function loadUsers() {
    $.ajax({
        url: "/api/admin/users",
        type: "GET",
        headers: getAuthorizationHeaders(),
        success: function (users) {
            applicationState.users = users;
            renderUsers();
        },
        error: handleRequestError
    });
}

function renderRoles() {
    const html = applicationState.roles.map(role => {
        const protectedRole = role.name === "ROLE_USER" || role.name === "ROLE_ADMIN" || role.name === "ROLE_TRANSLATOR" || role.name === "ROLE_SCANLATOR";
        const button = protectedRole
            ? `<button type="button" class="button button-secondary button-small" disabled>Системная</button>`
            : `<button type="button" class="button button-danger button-small" onclick="deleteRole(${role.id})">Удалить</button>`;

        return `
            <div class="role-row">
                <div>
                    <div class="role-name">${escapeHtml(role.name)}</div>
                    <div class="role-id">ID: ${role.id}</div>
                </div>
                ${button}
            </div>
        `;
    }).join("");

    $("#roles-container").html(html);
}

function renderUsers() {
    const html = applicationState.users.map(user => {
        const userRoles = Array.from(user.roles);
        const roleBadges = userRoles.map(roleName => {
            const role = applicationState.roles.find(item => item.name === roleName);
            const removeButton = role ? `
                <button type="button" class="role-remove-button" onclick="removeRole(${user.id}, ${role.id})">×</button>
            ` : "";
            return `<span class="role-badge">${escapeHtml(roleName)}${removeButton}</span>`;
        }).join("");

        const availableRoles = applicationState.roles.filter(role => !userRoles.includes(role.name));
        const roleOptions = availableRoles.map(role => `
            <option value="${role.id}">${escapeHtml(role.name)}</option>
        `).join("");

        return `
            <tr>
                <td>
                    <div class="user-name">${escapeHtml(user.fullName)}</div>
                    <div class="user-email">${escapeHtml(user.email)}</div>
                </td>
                <td>
                    <div class="role-list">${roleBadges}</div>
                </td>
                <td>
                    <div class="user-role-control">
                        <select id="role-select-${user.id}">
                            <option value="">Выберите роль</option>
                            ${roleOptions}
                        </select>
                        <button type="button" class="button button-primary button-small" onclick="assignSelectedRole(${user.id})">Назначить</button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    $("#users-table-body").html(html);
}

function createRole() {
    const roleName = $("#new-role-name").val().trim().toUpperCase();
    if (!roleName.startsWith("ROLE_")) {
        showMessage("Название должно начинаться с ROLE_", "warning");
        return;
    }

    $.ajax({
        url: "/api/admin/roles",
        type: "POST",
        contentType: "application/json",
        headers: getAuthorizationHeaders(),
        data: JSON.stringify({ name: roleName }),
        success: function () {
            $("#new-role-name").val("ROLE_");
            showMessage("Роль успешно добавлена", "success");
            loadAdminData();
        },
        error: handleRequestError
    });
}

function deleteRole(roleId) {
    if (!confirm("Удалить эту роль?")) return;

    $.ajax({
        url: "/api/admin/roles/" + roleId,
        type: "DELETE",
        headers: getAuthorizationHeaders(),
        success: function () {
            showMessage("Роль удалена", "success");
            loadAdminData();
        },
        error: handleRequestError
    });
}

function assignSelectedRole(userId) {
    const roleId = $("#role-select-" + userId).val();
    if (!roleId) {
        showMessage("Выберите роль", "warning");
        return;
    }

    $.ajax({
        url: "/api/admin/users/" + userId + "/roles/" + roleId,
        type: "POST",
        headers: getAuthorizationHeaders(),
        success: function () {
            showMessage("Роль назначена пользователю", "success");
            loadAdminData();
        },
        error: handleRequestError
    });
}

function removeRole(userId, roleId) {
    if (!confirm("Удалить роль у пользователя?")) return;

    $.ajax({
        url: "/api/admin/users/" + userId + "/roles/" + roleId,
        type: "DELETE",
        headers: getAuthorizationHeaders(),
        success: function () {
            showMessage("Роль удалена у пользователя", "success");
            loadAdminData();
        },
        error: handleRequestError
    });
}

// HELPERS
function clearRegisterForm() {
    $("#register-full-name").val("");
    $("#register-email").val("");
    $("#register-password").val("");
    $("#register-repeat-password").val("");
}

function handleRequestError(xhr) {
    if (xhr.status === 401) {
        removeToken();
        showGuestHeader();
        showAuthModal("login");
        showMessage("Сессия истекла. Войдите снова.", "warning");
        return;
    }

    if (xhr.status === 403) {
        showMessage("У вас нет доступа к этой операции", "danger");
        return;
    }

    const response = xhr.responseJSON;
    const message = response?.detail || response?.message || `Ошибка сервера: ${xhr.status}`;
    showMessage(message, "danger");
}

function showMessage(message, type) {
    $("#message-container").html(`
        <div class="alert alert-${type}">
            ${escapeHtml(message)}
        </div>
    `);
    setTimeout(() => {
        $("#message-container").empty();
    }, 5000);
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return $("<div>").text(String(value)).html();
}
