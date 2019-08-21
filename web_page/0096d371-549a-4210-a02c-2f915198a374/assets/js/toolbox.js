// Useful to escape HTML character of a variable that will be injected as the text of an HTML widget
function htmlEntities(str) {
  if(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  } else {
    return null;
  }
}