const API_BASE_URL = 'http://localhost:8080/api/analytics';

/**
 * Fetch active users count (last 5 minutes)
 */
export async function getActiveUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/active-users`);
        if (!response.ok) throw new Error('Failed to fetch active users');
        return await response.json();
    } catch (error) {
        console.error('Error fetching active users:', error);
        throw error;
    }
}

/**
 * Fetch top pages by view count (last 15 minutes)
 * @param {number} limit - Number of top pages to fetch
 */
export async function getTopPages(limit = 10) {
    try {
        const response = await fetch(`${API_BASE_URL}/top-pages?limit=${limit}`);
        if (!response.ok) throw new Error('Failed to fetch top pages');
        return await response.json();
    } catch (error) {
        console.error('Error fetching top pages:', error);
        throw error;
    }
}

/**
 * Fetch active sessions for a specific user
 * @param {string} userId - User ID to query
 */
export async function getActiveSessions(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/active-sessions?userId=${userId}`);
        if (!response.ok) throw new Error('Failed to fetch active sessions');
        return await response.json();
    } catch (error) {
        console.error('Error fetching active sessions:', error);
        throw error;
    }
}

/**
 * Fetch recent users with active sessions
 */
export async function getRecentSessions(limit = 5) {
    try {
        const response = await fetch(`${API_BASE_URL}/recent-sessions?limit=${limit}`);
        if (!response.ok) throw new Error('Failed to fetch recent sessions');
        return await response.json();
    } catch (error) {
        console.error('Error fetching recent sessions:', error);
        throw error;
    }
}
