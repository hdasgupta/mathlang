
$(document).ready(function() {
    $('.content').richText({
        bold: true,
        italic: true,
        underline: false,

        // text alignment
        leftAlign: false,
        centerAlign: false,
        rightAlign: false,
        justify: false,

        // lists
        ol: false,
        ul: false,

        // title
        heading: false,

        // fonts
        fonts: false,
        fontColor: true,
        fontSize: true,

        // uploads
        imageUpload: false,
        fileUpload: false,
        urls: false,

        // tables
        table: true,

        // code
        removeStyles: false,
        code: false,

        youtubeCookies: false,

        // preview
        preview: false,

        // placeholder
        placeholder: 'Enter Formula ...',

        // dev settings
        useSingleQuotes: false,
        height: 50,
        heightPercentage: 5,
        id: "",
        class: "",
        useParagraph: false,
        maxlength: 0,
        useTabForNext: false,

        // callback function after init
        callback: undefined,


    });

    $('.richText-toolbar').hide();
})