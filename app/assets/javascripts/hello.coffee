getBars = () ->
  $.get "/bars", (bars) ->
    $("#bars").empty()
    $.each bars, (index, bar) ->
      $("#bars").append $("<li>").text bar.name

$ ->
  getBars()
  $("#barForm").submit (event) ->
    event.preventDefault()
    $.ajax
      url: event.target.action
      type: event.target.method
      contentType: "application/json"
      data: JSON.stringify({name: $("#barName").val()})
      success: () ->
        getBars()
        $("#barName").val("")