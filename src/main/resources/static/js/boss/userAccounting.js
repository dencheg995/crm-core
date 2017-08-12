$(document).ready(function () {
    $('.formEditAllUsers').on('submit', function (event) {
        event.preventDefault();
        $('.errorMessage').html('').hide();
        var id = $(this).data('id');
        if (!isValidPasswordsData($('.editAllUsersOldPassword' + id).val(), $('.editAllUsersNewPassword' + id).val(), $('.editAllUsersRepeatedPassword' + id).val())) {
            return false;
        }
        var positionCheckboxId = '.editAllUsers-PositionCheckbox' + id;
        var positionsIds = $(positionCheckboxId + ' input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var rolesCheckboxId = '.editAllUsers-RoleCheckbox' + id;
        var rolesIds = $(rolesCheckboxId + ' input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var data = $(this).serializeArray();
        data.push({name: 'positionsIds', value: positionsIds}, {name: 'rolesIds', value: rolesIds});
        $.ajax({
            type: 'post',
            url: '/boss/user/edit',
            data: data,
            success: function (data) {
                location.reload();
            },
            error: function (error) {
                var errorMessage = '<h4 style="color:red;" align="center">' + error.responseText + '</h4>';
                $('.errorMessage').html(errorMessage).show();
            }
        })
    });
});


$(document).ready(function () {
    $('.formEditUserByPosition').on('submit', function (event) {
        event.preventDefault();
        $('.errorMessage').html('').hide();
        var id = $(this).data('id');
        if (!isValidPasswordsData($('.editUsersByPositionOldPassword' + id).val(), $('.editUsersByPositionNewPassword' + id).val(), $('.editUsersByPositionRepeatedPassword' + id).val())) {
            return false;
        }
        var positionCheckboxId = '.editUsersByPosition-PositionCheckbox' + id;
        var positionsIds = $(positionCheckboxId + ' input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var rolesCheckboxId = '.editUsersByPosition-RoleCheckbox' + id;
        var rolesIds = $(rolesCheckboxId + ' input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var data = $(this).serializeArray();
        data.push({name: 'positionsIds', value: positionsIds}, {name: 'rolesIds', value: rolesIds});
        $.ajax({
            type: 'post',
            url: '/boss/user/edit',
            data: data,
            success: function (data) {
                location.reload();
            },
            error: function (error) {
                var errorMessage = '<h4 style="color:red;" align="center">' + error.responseText + '</h4>';
                $('.errorMessage').html(errorMessage).show();
            }
        })
    });
});

function isValidPasswordsData(oldPassword, newPassword, repeatedPassword) {
    if (oldPassword == "" && newPassword == "" && repeatedPassword == "") {
        return true;
    }
    if (oldPassword == "") {
        var errorMessage = '<h4 style="color:red;" align="center">Не указан старый пароль</h4>';
        $('.errorMessage').html(errorMessage).show();
        return false;
    }
    if (newPassword == "") {
        var errorMessage = '<h4 style="color:red;" align="center">Не указан новый пароль</h4>';
        $('.errorMessage').html(errorMessage).show();
        return false;
    }
    if (repeatedPassword == "") {
        var errorMessage = '<h4 style="color:red;" align="center">Не указан повторный пароль</h4>';
        $('.errorMessage').html(errorMessage).show();
        return false;
    }
    if (newPassword != repeatedPassword) {
        var errorMessage = '<h4 style="color:red;" align="center">Новый пароль и повторный не совпадают</h4>';
        $('.errorMessage').html(errorMessage).show();
        return false;
    }
    return true;
}


$(document).ready(function () {
    $('.formAddUser').on('submit', function (event) {
        event.preventDefault();
        $('.errorMessage').html('').hide();
        if ($('.addUserFirstPassword').val() != $('.addUserSecondPassword').val()) {
            var errorMessage = '<h4 style="color:red;" align="center">Пароли не совпадают</h4>';
            $('.errorMessage').html(errorMessage).show();
            return false;
        }
        var positionsIds = $('.addUser-PositionCheckbox input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var rolesIds = $('.addUser-RoleCheckbox input:checkbox:checked').map(function () {
            return $(this).val();
        }).get();
        var data = $(this).serializeArray();
        data.push({name: 'positionsIds', value: positionsIds}, {name: 'rolesIds', value: rolesIds});
        $.ajax({
            type: 'post',
            url: '/boss/user/add',
            data: data,
            success: function (data) {
                location.reload();
            },
            error: function (error) {
                var errorMessage = '<h4 style="color:red;" align="center">' + error.responseText + '</h4>';
                $('.errorMessage').html(errorMessage).show();
            }
        })
    });
});

$(document).ready(function () {
    $('.formAddPosition').on('submit', function (event) {
        event.preventDefault();
        $('.errorMessage').html('').hide();
        var data = $(this).serialize();
        $.ajax({
            type: 'post',
            url: '/boss/user/position/add',
            data: data,
            success: function (data) {
                location.reload();
            },
            error: function (error) {
                var errorMessage = '<h4 style="color:red;" align="center">' + error.responseText + '</h4>';
                $('.errorMessage').html(errorMessage).show();
            }
        })
    });
});

$(document).ready(function () {
    $('.formEditPosition').on('submit', function (event) {
        event.preventDefault();
        $('.errorMessage').html('').hide();
        var data = $(this).serialize();
        $.ajax({
            type: 'post',
            url: '/boss/user/position/edit',
            data: data,
            success: function (data) {
                location.reload();
            },
            error: function (error) {
                var errorMessage = '<h4 style="color:red;" align="center">' + error.responseText + '</h4>';
                $('.errorMessage').html(errorMessage).show();
            }
        })
    });
});
