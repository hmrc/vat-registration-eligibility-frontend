var backLink = document.querySelector('.govuk-back-link[href="#"]')

if (backLink != null) {
    backLink.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    })
}