$(document).ready(function() {
    $('#registerBtn').click(function() {
        // Basic validation
        if ($('#password').val() !== $('#confirmPassword').val()) {
            alert('Passwords do not match.');
            return;
        }

        // Simulate registration and email sending
        alert('Registration successful! Sending verification email...');

        // Hide registration form and show email verification section
        $('#registrationForm').hide();
        $('#emailVerificationSection').show();
    });

    $('#verificationCompleteBtn').click(function() {
        // Simulate email verification
        alert('Email verified!');

        // Hide email verification section and show profile setup section
        $('#emailVerificationSection').hide();
        $('#profileSetupSection').show();
    });

    $('#profilePicture').change(function() {
        const file = this.files[0];
        if (file) {
            let reader = new FileReader();
            reader.onload = function(event) {
                $('#profilePreview').attr('src', event.target.result);
                $('#profilePreview').show();
            }
            reader.readAsDataURL(file);
        }
    });

    $('#profileSaveBtn').click(function() {
        // Simulate profile saving
        alert('Profile saved!');
    });
});