var backLink = document.querySelector('.govuk-back-link[href="#"]')

if (backLink != null) {
    backLink.addEventListener('click', function(e) {
        e.preventDefault();
        if (!backLink.hasAttribute('data-clicked')) {
                    backLink.setAttribute('data-clicked', 'true');
        window.history.back();
    })
}