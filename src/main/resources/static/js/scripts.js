// Initialize after the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add animation to cards
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.classList.add('animate-fade-in');
    });

    // Form validation enhancement
    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // Tooltip initialization (if using Bootstrap tooltips)
    const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltips.forEach(tooltip => {
        new bootstrap.Tooltip(tooltip);
    });

    // Event countdown timer
    const countdownElements = document.querySelectorAll('.event-countdown');
    countdownElements.forEach(countdownElement => {
        const eventDate = new Date(countdownElement.getAttribute('data-event-date')).getTime();
        
        // Update countdown every second
        const countdownInterval = setInterval(function() {
            // Get current date and time
            const now = new Date().getTime();
            
            // Find the time difference between now and the event date
            const timeLeft = eventDate - now;
            
            // If event has passed, clear interval and show expired message
            if (timeLeft < 0) {
                clearInterval(countdownInterval);
                countdownElement.innerHTML = "Event has started!";
                return;
            }
            
            // Calculate days, hours, minutes, seconds
            const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
            const hours = Math.floor((timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((timeLeft % (1000 * 60)) / 1000);
            
            // Display countdown
            countdownElement.innerHTML = `${days}d ${hours}h ${minutes}m ${seconds}s`;
        }, 1000);
    });

    // Toggle password visibility
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const passwordField = document.querySelector(this.getAttribute('data-target'));
            const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordField.setAttribute('type', type);
            
            // Toggle icon
            this.querySelector('i').classList.toggle('bi-eye');
            this.querySelector('i').classList.toggle('bi-eye-slash');
        });
    });
}); 