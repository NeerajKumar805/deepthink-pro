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

        // Simulate sending verification email (replace with actual API call)
        console.log('Sending verification email to: ' + email);
        $('#emailVerificationModal').modal('show');
    });

    $('#verifyEmailBtn').click(function() {
        var verificationCode = $('#verificationCode').val();
        // Simulate verification (replace with actual API call)
        console.log('Verifying code: ' + verificationCode);
        $('#emailVerificationModal').modal('hide');
        $('#profileSetupModal').modal('show');
    });

    $('#saveProfileBtn').click(function() {
        var bio = $('#bio').val();
        var profilePicture = $('#profilePicture').val();
        // Simulate saving profile (replace with actual API call)
        console.log('Saving profile with bio: ' + bio + ' and picture: ' + profilePicture);
        $('#profileSetupModal').modal('hide');
        alert('Profile saved successfully!');
    });
});