document.addEventListener('DOMContentLoaded', function() {
    // Check if user has admin role (this is a simple client-side check, security is handled server-side)
    const adminElements = document.querySelectorAll('[sec\\:authorize="hasRole(\'ADMIN\')"]');
    if (adminElements.length > 0) {
        // If admin, fetch unread message count
        fetchUnreadMessageCount();
        
        // Update unread messages count every 5 minutes
        setInterval(fetchUnreadMessageCount, 5 * 60 * 1000);
    }
    
    function fetchUnreadMessageCount() {
        fetch('/api/messages/unread-count')
            .then(response => response.json())
            .then(data => {
                updateUnreadBadge(data.count);
            })
            .catch(error => console.error('Error fetching unread message count:', error));
    }
    
    function updateUnreadBadge(count) {
        const badge = document.getElementById('unreadBadge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }
    }
}); 