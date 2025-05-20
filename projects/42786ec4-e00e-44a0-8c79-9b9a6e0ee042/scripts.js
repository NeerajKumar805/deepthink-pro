$(document).ready(function() {
    let emailVerified = false;

    $('#registerBtn').click(function() {
        // Basic validation (can be improved)
        if ($('#password').val() !== $('#confirmPassword').val()) {
            alert('Passwords do not match.');
            return;
        }

        // Simulate sending verification email (replace with actual API call)
        alert('Verification email sent to ' + $('#email').val() + '.');
        $('#registrationForm').hide();
        $('#verificationSection').show();
    });

    $('#verifyBtn').click(function() {
        // Simulate verification (replace with actual API call)
        emailVerified = true;
        alert('Email verified!');
        $('#verificationSection').hide();
        $('#profileSetupSection').show();
    });

    $('#saveProfileBtn').click(function() {
        if (!emailVerified) {
            alert('Please verify your email first.');
            return;
        }

        if (!$('#termsCheckbox').is(':checked')) {
            alert('Please agree to the Terms of Service and Privacy Policy.');
            return;
        }

        // Simulate saving profile (replace with actual API call)
        alert('Profile saved successfully!');
        // You can redirect to a dashboard or other page here
        window.location.href = 'https://www.example.com/dashboard'; // Replace with your actual dashboard URL
    });

    $('#profilePicture').change(function() {
        const file = this.files[0];
        if (file) {
            let reader = new FileReader();
            reader.onload = function(event) {
                $('#profilePreview').attr('src', event.target.result);
            }
            reader.readAsDataURL(file);
        }
    });
});