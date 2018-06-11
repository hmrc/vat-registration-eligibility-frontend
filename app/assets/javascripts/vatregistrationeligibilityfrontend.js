$(document).ready(function() {


  // Details/summary polyfill from frontend toolkit
  GOVUK.details.init()

  // =====================================================
  // Initialise show-hide-content
  // Toggles additional content based on radio/checkbox input state
  // =====================================================
  var showHideContent, mediaQueryList;
  showHideContent = new GOVUK.ShowHideContent()
  showHideContent.init()

  initRadioOptions();
});

var initRadioOptions = function () {
  var radioOptions = $('input[type="radio"]');

  radioOptions.each(function () {
    var o = $(this).parent().next('.additional-option-block');
    if ($(this).prop('checked')) {
        o.show();
    } else {
        o.hide();
    }
  });

  radioOptions.on('click', function (e) {
    $('.additional-option-block').hide();
    var o = $(this).parent().next('.additional-option-block');
    if (o.index() != -1) {
        o.show();
    }
  });
}