import { useState, useEffect } from 'react';
import TopPagesChart from './components/TopPagesChart';
import SessionsTable from './components/SessionsTable';
import { getActiveUsers, getTopPages, getRecentSessions } from './services/analyticsApi';
import './App.css';

function App() {
    const [activeUsers, setActiveUsers] = useState(0);
    const [topPages, setTopPages] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [lastUpdated, setLastUpdated] = useState(null);

    // Fetch all analytics data
    const fetchData = async () => {
        try {
            setError(null);

            const usersData = await getActiveUsers();
            setActiveUsers(usersData.activeUsers);

            const pagesData = await getTopPages(10);
            setTopPages(pagesData.pages);

            const sessionsData = await getRecentSessions(5);
            setSessions(sessionsData.users || []);

            setLastUpdated(new Date());
            setLoading(false);
        } catch (err) {
            setError('Failed to fetch analytics data. Make sure the backend is running.');
            console.error(err);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // Auto-refresh every 5 seconds
    useEffect(() => {
        const interval = setInterval(fetchData, 5000);
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return (
            <div className="app">
                <div className="loading">
                    <div className="spinner"></div>
                    <p>Loading analytics...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="app">
            {/* Header */}
            <header className="header">
                <div className="container">
                    <h1>📊 Analytics Dashboard</h1>
                    <div className="header-info">
                        {error && <div className="error-badge">{error}</div>}
                        {lastUpdated && !error && (
                            <div className="last-updated">
                                Last updated: {lastUpdated.toLocaleTimeString()}
                                <span className="refresh-dot"></span>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="main">
                <div className="container">
                    {/* Metrics Grid */}
                    <div className="metrics-grid">
                        {/* Left Column - Active Users + Sessions */}
                        <div className="left-column">
                            {/* Active Users Card */}
                            <div className="card metric-card">
                                <h3>Active Users</h3>
                                <p className="subtitle">Last 5 minutes</p>
                                <div className="metric-value">
                                    {activeUsers}
                                </div>
                            </div>

                            {/* Sessions Card */}
                            <div className="card table-card">
                                <h3>Active Sessions</h3>
                                <p className="subtitle">Most recent users with active sessions</p>
                                <SessionsTable sessions={sessions} />
                            </div>
                        </div>

                        {/* Right Column - Top Pages */}
                        <div className="card chart-card">
                            <h3>Top Pages</h3>
                            <p className="subtitle">Last 15 minutes - Most visited pages</p>
                            <TopPagesChart pages={topPages} />
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default App;
