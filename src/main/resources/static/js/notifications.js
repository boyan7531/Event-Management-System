/**
 * Notifications handler
 */
$(document).ready(function() {
    // Check for new notifications every 30 seconds
    fetchUnreadNotifications();
    setInterval(fetchUnreadNotifications, 30000);
    
    // Mark all notifications as read
    $('#markAllRead').on('click', function() {
        $.ajax({
            url: '/notifications/mark-all-read',
            type: 'POST',
            success: function() {
                updateNotificationBadge(0);
                $('.notification-item').removeClass('unread');
                $('.notification-item-indicator').hide();
            }
        });
    });
    
    // Handle clicking on a notification
    $(document).on('click', '.notification-item', function(e) {
        const notificationId = $(this).data('notification-id');
        
        // Only mark as read if clicking the notification itself, not any buttons inside it
        if (!$(e.target).closest('button').length) {
            $.ajax({
                url: '/notifications/mark-read/' + notificationId,
                type: 'POST',
                success: function() {
                    fetchUnreadNotifications();
                }
            });
        }
    });
    
    // Handle deleting a notification
    $(document).on('click', '.delete-notification', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        const notificationId = $(this).data('notification-id');
        const listItem = $(this).closest('.notification-item');
        
        $.ajax({
            url: '/notifications/delete/' + notificationId,
            type: 'POST',
            success: function() {
                listItem.fadeOut(300, function() {
                    $(this).remove();
                    
                    // Check if there are any notifications left
                    if ($('.notification-item').length === 0) {
                        $('#notificationList').html(
                            '<div class="text-center py-4 notification-empty">' +
                            '<i class="bi bi-bell text-muted fs-3"></i>' +
                            '<p class="text-muted mt-2">No new notifications</p>' +
                            '</div>'
                        );
                    }
                    
                    // Update unread count
                    fetchUnreadNotifications();
                });
            }
        });
    });
});

/**
 * Fetch unread notifications from the server
 */
function fetchUnreadNotifications() {
    $.ajax({
        url: '/notifications/unread-count',
        type: 'GET',
        success: function(data) {
            updateNotificationBadge(data.count);
        }
    });
    
    $.ajax({
        url: '/notifications/unread',
        type: 'GET',
        success: function(notifications) {
            renderNotifications(notifications);
        }
    });
}

/**
 * Update the notification badge with the count of unread notifications
 */
function updateNotificationBadge(count) {
    if (count > 0) {
        $('#notificationCount').text(count);
        $('#notificationBadge').show();
    } else {
        $('#notificationBadge').hide();
    }
}

/**
 * Render notifications in the dropdown
 */
function renderNotifications(notifications) {
    if (notifications.length === 0) {
        $('#notificationList').html(
            '<div class="text-center py-4 notification-empty">' +
            '<i class="bi bi-bell text-muted fs-3"></i>' +
            '<p class="text-muted mt-2">No new notifications</p>' +
            '</div>'
        );
        return;
    }
    
    let html = '';
    
    notifications.forEach(function(notification) {
        let icon = '';
        
        // Set icon based on notification type
        switch(notification.type) {
            case 'EVENT_APPROVED':
                icon = '<i class="bi bi-check-circle-fill text-success"></i>';
                break;
            case 'EVENT_REJECTED':
                icon = '<i class="bi bi-x-circle-fill text-danger"></i>';
                break;
            case 'EVENT_CANCELED':
                icon = '<i class="bi bi-slash-circle-fill text-warning"></i>';
                break;
            case 'NEW_EVENT_PENDING':
                icon = '<i class="bi bi-hourglass-split text-primary"></i>';
                break;
            case 'NEW_MESSAGE':
                icon = '<i class="bi bi-envelope-fill text-info"></i>';
                break;
            case 'SYSTEM':
            default:
                icon = '<i class="bi bi-gear-fill text-secondary"></i>';
                break;
        }
        
        // Format date
        let formattedDate = "Just now";
        try {
            if (notification.createdAt) {
                const date = new Date(notification.createdAt);
                if (!isNaN(date.getTime())) {
                    formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                }
            }
        } catch (e) {
            console.error("Error formatting date:", e);
        }
        
        html += `
            <div class="notification-item unread p-3 border-bottom position-relative" data-notification-id="${notification.id}">
                <div class="notification-item-indicator position-absolute bg-primary" style="width: 4px; height: 100%; left: 0; top: 0;"></div>
                <div class="d-flex">
                    <div class="me-3">${icon}</div>
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between">
                            <p class="mb-1 text-white">${notification.message}</p>
                            <button class="delete-notification btn btn-sm" data-notification-id="${notification.id}">
                                <i class="bi bi-x"></i>
                            </button>
                        </div>
                        <div class="d-flex justify-content-between align-items-center">
                            <small class="text-white-50">${formattedDate}</small>
                            ${notification.link ? `<a href="${notification.link}" class="btn btn-sm btn-link text-white p-0">View</a>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    $('#notificationList').html(html);
} 