const BASE_URL = 'http://localhost:8080';

// ─── Token Management ───────────────────────────────────────────────────────
const Auth = {
    getToken: () => localStorage.getItem('token'),
    getUser: () => JSON.parse(localStorage.getItem('user') || 'null'),
    getRole: () => {
        const user = Auth.getUser();
        return user ? user.role : null;
    },
    setSession: (token, user) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
    },
    clear: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },
    isLoggedIn: () => !!localStorage.getItem('token'),
    requireAuth: () => {
        if (!Auth.isLoggedIn()) {
            window.location.href = 'login.html';
            return false;
        }
        return true;
    },
    redirectByRole: () => {
        const role = Auth.getRole();
        if (role === 'ADMIN') window.location.href = 'dashboard-admin.html';
        else if (role === 'INSTRUCTOR') window.location.href = 'dashboard-instructor.html';
        else if (role === 'STUDENT') window.location.href = 'dashboard-student.html';
        else window.location.href = 'index.html';
    }
};

// ─── HTTP Helper ─────────────────────────────────────────────────────────────
async function request(method, path, body = null, auth = true) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth && Auth.getToken()) headers['Authorization'] = `Bearer ${Auth.getToken()}`;
    const config = { method, headers };
    if (body) config.body = JSON.stringify(body);
    try {
        const res = await fetch(BASE_URL + path, config);
        if (res.status === 401) { Auth.clear(); window.location.href = 'login.html'; return; }
        const data = res.headers.get('content-type')?.includes('application/json') ? await res.json() : await res.text();
        if (!res.ok) throw { status: res.status, message: data?.message || data || 'Request failed' };
        return data;
    } catch (e) {
        if (e.status) throw e;
        throw { status: 0, message: 'Network error. Is the backend running?' };
    }
}

// ─── API Endpoints ────────────────────────────────────────────────────────────
const API = {
    // Auth
    login: (data) => request('POST', '/api/auth/login', data, false),
    register: (data) => request('POST', '/api/auth/register', data, false),

    // Courses
    getAllCourses: () => request('GET', '/api/courses'),
    getCourse: (id) => request('GET', `/api/courses/${id}`),
    createCourse: (data) => request('POST', '/api/courses', data),
    updateCourse: (id, data) => request('PUT', `/api/courses/${id}`, data),
    deleteCourse: (id) => request('DELETE', `/api/courses/${id}`),
    getMyCourses: () => request('GET', '/api/courses/my'),

    // Enrollments
    enroll: (courseId) => request('POST', '/api/enrollments', { courseId }),
    getMyEnrollments: () => request('GET', '/api/enrollments/my'),
    getAllEnrollments: () => request('GET', '/api/enrollments'),

    // Quiz
    getQuizzesByCourse: (courseId) => request('GET', `/api/quizzes/course/${courseId}`),
    createQuiz: (data) => request('POST', '/api/quizzes', data),
    updateQuiz: (id, data) => request('PUT', `/api/quizzes/${id}`, data),
    deleteQuiz: (id) => request('DELETE', `/api/quizzes/${id}`),
    submitQuiz: (data) => request('POST', '/api/quizzes/submit', data),

    // Users (Admin)
    getAllUsers: () => request('GET', '/api/users'),
    deleteUser: (id) => request('DELETE', `/api/users/${id}`),
    updateUserRole: (id, role) => request('PUT', `/api/users/${id}/role`, { role }),
};