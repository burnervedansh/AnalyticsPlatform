import React from 'react';
import './TopPagesChart.css';

/**
 * Simple horizontal bar chart for displaying top pages
 */
function TopPagesChart({ pages }) {
    if (!pages || pages.length === 0) {
        return (
            <div className="chart-empty">
                <p>No data available</p>
            </div>
        );
    }

    const maxViews = Math.max(...pages.map(p => p.views));

    return (
        <div className="top-pages-chart">
            {pages.map((page, index) => (
                <div key={index} className="chart-bar">
                    <div className="bar-label">{page.url}</div>
                    <div className="bar-container">
                        <div
                            className="bar-fill"
                            style={{ width: `${(page.views / maxViews) * 100}%` }}
                        >
                            <span className="bar-value">{page.views}</span>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}

export default TopPagesChart;
