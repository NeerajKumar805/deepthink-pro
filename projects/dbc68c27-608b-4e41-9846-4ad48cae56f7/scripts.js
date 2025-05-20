$(document).ready(function() {
    $('#registerBtn').click(function() {
        var username = $('#username').val();
        var email = $('#email').val();
        var password = $('#password').val();
        var confirmPassword = $('#confirmPassword').val();
        var phone = $('#phone').val();

        if (password !== confirmPassword) {
            alert('Passwords do not match.');
            return;
        }

        // Simulate email verification
        alert('Registration successful! Please check your email for verification.');
        $('#registrationForm').hide();
        $('#emailVerification').show();
    });

    $('#verificationComplete').click(function() {
        $('#emailVerification').hide();
        $('#profileSetup').show();
    });

    $('#profileComplete').click(function() {
        var profilePicture = $('#profilePicture').val();
        var bio = $('#bio').val();

        alert('Profile setup complete!');
        // You would typically redirect to a profile page here
        window.location.href = "#"; // Replace with your profile page URL
    });

    // Example of adding images dynamically (not required, but demonstrates functionality)
    // You'd likely fetch these from a server in a real application
    var images = [
        "https://via.placeholder.com/50",
        "https://via.placeholder.com/100",
        "https://via.placeholder.com/75",
        "https://via.placeholder.com/25"
    ];

    // Add images to the body (hidden by default)
    for (var i = 0; i < images.length; i++) {
        $('body').append('<img src="' + images[i] + '" class="hidden-image" alt="Dynamic Image">');
    }
});