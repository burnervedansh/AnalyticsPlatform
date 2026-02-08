import React from 'react';
import './SessionsTable.css';

/**
 * Simple table to display user sessions
 */
function SessionsTable({ sessions }) {
    if (!sessions || sessions.length === 0) {
        return (
            <div className="table-empty">
                <p>No active sessions</p>
            </div>
        );
    }

    return (
        <div className="sessions-table">
            <table>
                <thead>
                    <tr>
                        <th>User ID</th>
                        <th>Active Sessions</th>
                        <th>Session IDs</th>
                    </tr>
                </thead>
                <tbody>
                    {sessions.map((session, index) => (
                        <tr key={index}>
                            <td className="user-id">{session.userId}</td>
                            <td className="session-count">
                                <span className="badge">{session.activeSessions}</span>
                            </td>
                            <td className="session-ids">
                                {session.sessions.slice(0, 2).join(', ')}
                                {session.sessions.length > 2 && ` +${session.sessions.length - 2} more`}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default SessionsTable;
