$(document).ready(function() {
    let userEmail;

    $('#registerBtn').click(function() {
        const username = $('#username').val();
        userEmail = $('#email').val();
        const password = $('#password').val();
        const confirmPassword = $('#confirmPassword').val();
        const phone = $('#phone').val();

        if (password !== confirmPassword) {
            alert('Passwords do not match.');
            return;
        }

        // Simulate sending verification email
        alert('Verification email sent to: ' + userEmail);

        $('#verificationModal').modal('show');
    });

    $('#verifyBtn').click(function() {
        const verificationCode = $('#verificationCode').val();

        // Simulate verification process
        if (verificationCode === '123456') { // Replace with actual verification logic
            alert('Email verified!');
            $('#verificationModal').modal('hide');
            $('#profileModal').modal('show');
        } else {
            alert('Invalid verification code.');
        }
    });

    $('#saveProfileBtn').click(function() {
        const bio = $('#bio').val();
        const profilePicture = $('#profilePicture').val();

        // Simulate saving profile data
        alert('Profile saved!');
        $('#profileModal').modal('hide');
        // Optionally, redirect to a profile page or display a success message
    });
});